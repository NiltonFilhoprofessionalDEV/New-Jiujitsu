package br.com.academia.api.billing;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.student.Student;
import br.com.academia.api.student.StudentCreatedEvent;
import br.com.academia.api.student.StudentDeletingEvent;
import br.com.academia.api.student.StudentEnums.Kind;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Reações do financeiro ao ciclo de vida do aluno, sem o pacote de alunos depender deste. */
@Component
class StudentBillingListener {

	private static final int LEGAL_AGE = 18;

	private final PlanRepository plans;
	private final InvoiceRepository invoices;
	private final SubscriptionService subscriptions;
	private final Clock clock;

	StudentBillingListener(PlanRepository plans, InvoiceRepository invoices, SubscriptionService subscriptions,
			Clock clock) {
		this.plans = plans;
		this.invoices = invoices;
		this.subscriptions = subscriptions;
		this.clock = clock;
	}

	/** Todo aluno novo que treina entra no plano padrão, por qualquer caminho de cadastro. */
	@EventListener
	void onCreated(StudentCreatedEvent event) {
		Student student = event.student();
		if (student.getKind() != Kind.TRAINING) {
			return;
		}
		defaultFor(student).ifPresent(plan -> subscriptions.linkDefault(student, plan));
	}

	/** Faturas são histórico financeiro: quem já foi cobrado é desativado, não excluído. */
	@EventListener
	void onDeleting(StudentDeletingEvent event) {
		if (invoices.existsByStudentId(event.student().getId())) {
			throw ApiException.conflict("STUDENT_HAS_INVOICES",
					"O aluno tem faturas registradas; desative a matrícula em vez de remover");
		}
	}

	/** Menor de 18 usa o padrão kids, se houver; sem data de nascimento, conta como adulto. */
	private Optional<Plan> defaultFor(Student student) {
		LocalDate birth = student.getBirthDate();
		boolean minor = birth != null && Period.between(birth, LocalDate.now(clock)).getYears() < LEGAL_AGE;
		Optional<Plan> kids = minor
				? plans.findByAcademyIdAndIsKidsDefaultTrue(student.getAcademyId())
				: Optional.empty();
		return kids.or(() -> plans.findByAcademyIdAndIsDefaultTrue(student.getAcademyId()));
	}
}
