package br.com.academia.api.billing;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import br.com.academia.api.billing.InvoiceDtos.InvoiceFilter;
import br.com.academia.api.billing.InvoiceDtos.InvoiceList;
import br.com.academia.api.billing.InvoiceDtos.InvoiceRow;
import br.com.academia.api.billing.InvoiceDtos.PayRequest;
import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.PageResponse;
import br.com.academia.api.student.Student;
import br.com.academia.api.student.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Baixa manual (sem gateway): o gestor registra quando e como recebeu. */
@Service
public class InvoiceService {

	private final InvoiceRepository invoices;
	private final InvoiceQuery query;
	private final StudentService students;
	private final Clock clock;

	public InvoiceService(InvoiceRepository invoices, InvoiceQuery query, StudentService students, Clock clock) {
		this.invoices = invoices;
		this.query = query;
		this.students = students;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public InvoiceList list(UUID academyId, InvoiceFilter filter, int page, int size) {
		LocalDate today = today();
		long total = query.count(academyId, filter, today);
		List<InvoiceRow> rows = query.page(academyId, filter, today, page, size);
		int totalPages = (int) ((total + size - 1) / size);
		return new InvoiceList(new PageResponse<>(rows, total, totalPages, page, size),
				query.totals(academyId, filter, today), query.overdue(academyId, today));
	}

	/** Para a área do aluno: o acesso ao aluno já foi validado por quem chama. */
	@Transactional(readOnly = true)
	public List<InvoiceRow> forStudent(UUID academyId, UUID studentId, int limit) {
		return query.forStudent(academyId, studentId, today(), limit);
	}

	@Transactional
	public InvoiceRow pay(UUID academyId, UUID recordedBy, UUID id, PayRequest request) {
		if (request.paidOn().isAfter(today())) {
			throw ApiException.badRequest("PAYMENT_IN_FUTURE", "A data do recebimento não pode ser futura");
		}
		return change(academyId, id, i -> i.markPaid(request.paidOn(), request.method(), recordedBy));
	}

	@Transactional
	public InvoiceRow cancel(UUID academyId, UUID id) {
		return change(academyId, id, Invoice::cancel);
	}

	@Transactional
	public InvoiceRow reverse(UUID academyId, UUID id) {
		return change(academyId, id, Invoice::reversePayment);
	}

	private InvoiceRow change(UUID academyId, UUID id, Consumer<Invoice> action) {
		Invoice invoice = invoices.findForUpdate(id, academyId)
				.orElseThrow(() -> ApiException.notFound("INVOICE_NOT_FOUND", "Fatura não encontrada"));
		action.accept(invoice);
		Student student = students.require(academyId, invoice.getStudentId());
		return new InvoiceRow(invoice.getId(), student.getId(), student.getName(), invoice.getDescription(),
				invoice.getAmountCents(), invoice.getDueDate(),
				InvoiceQuery.situation(invoice.getStatus(), invoice.getDueDate(), today()),
				invoice.getPaidOn(), invoice.getPaymentMethod());
	}

	private LocalDate today() {
		return LocalDate.now(clock);
	}
}
