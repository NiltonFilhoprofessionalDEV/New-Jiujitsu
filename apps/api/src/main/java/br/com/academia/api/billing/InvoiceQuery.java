package br.com.academia.api.billing;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import br.com.academia.api.billing.Invoice.PaymentMethod;
import br.com.academia.api.billing.InvoiceDtos.Bucket;
import br.com.academia.api.billing.InvoiceDtos.InvoiceFilter;
import br.com.academia.api.billing.InvoiceDtos.InvoiceRow;
import br.com.academia.api.billing.InvoiceDtos.OverdueSummary;
import br.com.academia.api.billing.InvoiceDtos.Situation;
import br.com.academia.api.billing.InvoiceDtos.SituationFilter;
import br.com.academia.api.billing.InvoiceDtos.Totals;
import br.com.academia.api.common.TextNormalizer;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Lista e somas de faturas com o nome do aluno; a busca usa o nome já normalizado ({@code search_name}). */
@Repository
class InvoiceQuery {

	private static final String FROM = """
			from invoices i join students s on s.id = i.student_id
			where i.academy_id = :academyId
			  and (cast(:from as date) is null or i.due_date between cast(:from as date) and cast(:to as date))
			  and (cast(:pattern as text) is null or s.search_name like cast(:pattern as text) escape '\\')
			""";
	private static final String OPEN = "i.status = 'OPEN' and i.due_date >= :today";
	private static final String OVERDUE = "i.status = 'OPEN' and i.due_date < :today";
	private static final String PAID = "i.status = 'PAID'";

	private final JdbcClient jdbc;

	InvoiceQuery(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	List<InvoiceRow> page(UUID academyId, InvoiceFilter filter, LocalDate today, int page, int size) {
		String sql = """
				select i.id, i.student_id, s.name, i.description, i.amount_cents, i.due_date, i.status,
				       i.paid_on, i.payment_method
				""" + FROM + " and " + clause(filter.situation()) + """

				order by i.due_date desc, s.search_name, i.id
				limit :limit offset :offset
				""";
		return params(jdbc.sql(sql), academyId, filter, today)
				.param("limit", size)
				.param("offset", (long) page * size)
				.query((rs, n) -> row(rs, today))
				.list();
	}

	long count(UUID academyId, InvoiceFilter filter, LocalDate today) {
		String sql = "select count(*) " + FROM + " and " + clause(filter.situation());
		return params(jdbc.sql(sql), academyId, filter, today).query(Long.class).single();
	}

	Totals totals(UUID academyId, InvoiceFilter filter, LocalDate today) {
		String sql = "select " + bucket(OPEN) + ", " + bucket(OVERDUE) + ", " + bucket(PAID) + " " + FROM;
		return params(jdbc.sql(sql), academyId, filter, today)
				.query((rs, n) -> new Totals(new Bucket(rs.getLong(1), rs.getLong(2)),
						new Bucket(rs.getLong(3), rs.getLong(4)), new Bucket(rs.getLong(5), rs.getLong(6))))
				.single();
	}

	OverdueSummary overdue(UUID academyId, LocalDate today) {
		return jdbc.sql("""
				select count(distinct i.student_id), coalesce(sum(i.amount_cents), 0) from invoices i
				where i.academy_id = :academyId and i.status = 'OPEN' and i.due_date < :today
				""")
				.param("academyId", academyId)
				.param("today", today)
				.query((rs, n) -> new OverdueSummary(rs.getLong(1), rs.getLong(2)))
				.single();
	}

	/** Só constantes desta classe entram no SQL; valores do usuário vão sempre por parâmetro. */
	private static String clause(SituationFilter situation) {
		return switch (situation) {
			case ALL -> "true";
			case OPEN -> OPEN;
			case OVERDUE -> OVERDUE;
			case PAID -> PAID;
			case CANCELED -> "i.status = 'CANCELED'";
		};
	}

	private static String bucket(String condition) {
		return "count(*) filter (where " + condition + "), coalesce(sum(i.amount_cents) filter (where "
				+ condition + "), 0)";
	}

	private static JdbcClient.StatementSpec params(JdbcClient.StatementSpec spec, UUID academyId,
			InvoiceFilter filter, LocalDate today) {
		String folded = TextNormalizer.fold(filter.q());
		return spec.param("academyId", academyId)
				.param("today", today)
				.param("from", filter.month() == null ? null : filter.month().atDay(1))
				.param("to", filter.month() == null ? null : filter.month().atEndOfMonth())
				.param("pattern", folded.isEmpty() ? null : TextNormalizer.likeContains(folded));
	}

	private static InvoiceRow row(ResultSet rs, LocalDate today) throws SQLException {
		LocalDate due = rs.getObject("due_date", LocalDate.class);
		String method = rs.getString("payment_method");
		return new InvoiceRow(
				rs.getObject("id", UUID.class),
				rs.getObject("student_id", UUID.class),
				rs.getString("name"),
				rs.getString("description"),
				rs.getInt("amount_cents"),
				due,
				situation(Invoice.Status.valueOf(rs.getString("status")), due, today),
				rs.getObject("paid_on", LocalDate.class),
				method == null ? null : PaymentMethod.valueOf(method));
	}

	static Situation situation(Invoice.Status status, LocalDate due, LocalDate today) {
		return switch (status) {
			case OPEN -> due.isBefore(today) ? Situation.OVERDUE : Situation.OPEN;
			case PAID -> Situation.PAID;
			case CANCELED -> Situation.CANCELED;
		};
	}
}
