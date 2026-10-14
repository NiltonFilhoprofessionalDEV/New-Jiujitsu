package br.com.academia.api.billing;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import br.com.academia.api.student.StudentEnums.Status;
import br.com.academia.api.student.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Emite as faturas cujo vencimento entrou na janela de emissão. Matrícula inativa pausa a cobrança:
 * os ciclos que vencem enquanto ela está inativa são pulados, sem gerar dívida retroativa ao reativar.
 */
@Service
public class BillingCycle {

	static final int ISSUE_DAYS_AHEAD = 10;
	/** Teto de ciclos por execução, contra laço longo com uma data antiga. */
	private static final int MAX_CYCLES_PER_RUN = 24;
	private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("MM/yyyy");

	private final SubscriptionRepository subscriptions;
	private final PlanRepository plans;
	private final InvoiceRepository invoices;
	private final StudentRepository students;
	private final Clock clock;

	public BillingCycle(SubscriptionRepository subscriptions, PlanRepository plans, InvoiceRepository invoices,
			StudentRepository students, Clock clock) {
		this.subscriptions = subscriptions;
		this.plans = plans;
		this.invoices = invoices;
		this.students = students;
		this.clock = clock;
	}

	/** Usado pela rotina diária: cada mensalidade numa transação própria, para uma falha não travar as outras. */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int issueDue(UUID subscriptionId) {
		return subscriptions.findActiveForUpdate(subscriptionId)
				.map(s -> issue(s, LocalDate.now(clock)))
				.orElse(0);
	}

	/** Chamado por quem já tem a mensalidade travada na própria transação (vincular ou editar). */
	int issue(Subscription s, LocalDate today) {
		Plan plan = plans.findById(s.getPlanId()).orElseThrow();
		boolean active = students.findById(s.getStudentId())
				.map(st -> st.getStatus() == Status.ACTIVE)
				.orElse(false);
		LocalDate horizon = today.plusDays(ISSUE_DAYS_AHEAD);
		int issued = 0;
		for (int i = 0; i < MAX_CYCLES_PER_RUN && !s.getNextDueDate().isAfter(horizon); i++) {
			if (!active && !s.getNextDueDate().isBefore(today)) {
				break;
			}
			int amount = s.amountCents(plan);
			if (active && amount > 0) {
				invoices.save(Invoice.forCycle(s, description(plan, s.getNextDueDate()), amount));
				issued++;
			}
			s.advance(plan.getIntervalMonths());
		}
		return issued;
	}

	private static String description(Plan plan, LocalDate due) {
		return "Mensalidade " + plan.getName() + " · " + due.format(MONTH);
	}
}
