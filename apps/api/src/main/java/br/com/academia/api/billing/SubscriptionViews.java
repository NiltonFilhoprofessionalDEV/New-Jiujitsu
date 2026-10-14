package br.com.academia.api.billing;

import java.time.LocalDate;

import br.com.academia.api.billing.SubscriptionDtos.PlanRef;
import br.com.academia.api.billing.SubscriptionDtos.StudentRef;
import br.com.academia.api.billing.SubscriptionDtos.SubscriptionView;
import br.com.academia.api.student.Student;

final class SubscriptionViews {

	private SubscriptionViews() {
	}

	/** {@code balance} nulo: o aluno não tem fatura em aberto. */
	static SubscriptionView of(Subscription s, Plan plan, Student student, OpenBalance balance) {
		LocalDate nextDue = s.getNextDueDate();
		LocalDate overdueSince = null;
		long overdueCents = 0;
		if (balance != null) {
			overdueSince = balance.overdueSince();
			overdueCents = balance.overdueCents();
			if (balance.upcomingDue() != null && balance.upcomingDue().isBefore(nextDue)) {
				nextDue = balance.upcomingDue();
			}
		}
		return new SubscriptionView(
				s.getId(),
				new StudentRef(student.getId(), student.getName(), student.getNickname(), student.getStatus()),
				new PlanRef(plan.getId(), plan.getName(), plan.getPriceCents(), plan.getIntervalMonths()),
				s.getDueDay(),
				s.getDiscountType(),
				s.getDiscountValue(),
				s.getDiscountReason(),
				s.amountCents(plan),
				nextDue,
				s.getStartedOn(),
				overdueSince,
				overdueCents);
	}
}
