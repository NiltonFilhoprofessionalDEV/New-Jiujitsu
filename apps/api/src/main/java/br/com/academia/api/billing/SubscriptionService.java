package br.com.academia.api.billing;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import br.com.academia.api.billing.Subscription.Terms;
import br.com.academia.api.billing.SubscriptionDtos.LinkRequest;
import br.com.academia.api.billing.SubscriptionDtos.SubscriptionView;
import br.com.academia.api.billing.SubscriptionDtos.TermsRequest;
import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.TextNormalizer;
import br.com.academia.api.student.Student;
import br.com.academia.api.student.StudentRepository;
import br.com.academia.api.student.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Mudanças de plano, dia ou desconto valem para as próximas faturas; as já emitidas não mudam. */
@Service
public class SubscriptionService {

	private static final int MAX_FIRST_DUE_MONTHS = 12;

	private final SubscriptionRepository subscriptions;
	private final PlanService plans;
	private final PlanRepository planRepository;
	private final InvoiceRepository invoices;
	private final StudentService students;
	private final StudentRepository studentRepository;
	private final BillingCycle cycle;
	private final Clock clock;

	public SubscriptionService(SubscriptionRepository subscriptions, PlanService plans, PlanRepository planRepository,
			InvoiceRepository invoices, StudentService students, StudentRepository studentRepository,
			BillingCycle cycle, Clock clock) {
		this.subscriptions = subscriptions;
		this.plans = plans;
		this.planRepository = planRepository;
		this.invoices = invoices;
		this.students = students;
		this.studentRepository = studentRepository;
		this.cycle = cycle;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public List<SubscriptionView> list(UUID academyId) {
		List<Subscription> active = subscriptions.findByAcademyIdAndCanceledAtIsNull(academyId);
		Map<UUID, Student> byStudent = studentRepository.findAllById(active.stream().map(Subscription::getStudentId)
				.toList()).stream().collect(Collectors.toMap(Student::getId, Function.identity()));
		Map<UUID, Plan> byPlan = planRepository.findByAcademyIdOrderByNameAsc(academyId).stream()
				.collect(Collectors.toMap(Plan::getId, Function.identity()));
		Map<UUID, OpenBalance> balances = balances(academyId);
		return active.stream()
				.map(s -> SubscriptionViews.of(s, byPlan.get(s.getPlanId()), byStudent.get(s.getStudentId()),
						balances.get(s.getStudentId())))
				.sorted(Comparator.comparing(v -> TextNormalizer.fold(v.student().name())))
				.toList();
	}

	@Transactional
	public SubscriptionView link(UUID academyId, LinkRequest request) {
		Student student = students.require(academyId, request.studentId());
		Plan plan = requireLinkable(academyId, request.terms().planId());
		Terms terms = toTerms(plan, request.terms());
		LocalDate first = firstDue(request.firstDueDate(), terms.dueDay());
		return view(start(student, plan, terms, first), plan, student);
	}

	/** Vinculação automática de um aluno novo ao plano padrão, sem desconto e no dia do plano. */
	void linkDefault(Student student, Plan plan) {
		Terms terms = new Terms(plan.getId(), plan.getDueDay(), null, null, null);
		start(student, plan, terms, DueDates.nextOccurrence(plan.getDueDay(), today()));
	}

	@Transactional
	public SubscriptionView update(UUID academyId, UUID id, TermsRequest request) {
		Subscription s = subscriptions.findActiveForUpdate(id, academyId)
				.orElseThrow(() -> ApiException.notFound("SUBSCRIPTION_NOT_FOUND", "Mensalidade não encontrada"));
		Plan plan = request.planId().equals(s.getPlanId())
				? plans.require(academyId, request.planId())
				: requireLinkable(academyId, request.planId());
		Terms terms = toTerms(plan, request);
		boolean dayChanged = terms.dueDay() != s.getDueDay();
		s.apply(terms);
		if (dayChanged) {
			s.moveNextDue(DueDates.moveToDay(s.getNextDueDate(), terms.dueDay(), today()));
		}
		cycle.issue(s, today());
		return view(s, plan, students.require(academyId, s.getStudentId()));
	}

	/** Encerra a cobrança; faturas já emitidas continuam valendo e podem ser baixadas ou canceladas. */
	@Transactional
	public void cancel(UUID academyId, UUID id) {
		subscriptions.findActiveForUpdate(id, academyId)
				.orElseThrow(() -> ApiException.notFound("SUBSCRIPTION_NOT_FOUND", "Mensalidade não encontrada"))
				.cancel();
	}

	private Subscription start(Student student, Plan plan, Terms terms, LocalDate firstDue) {
		student.requireTraining();
		if (subscriptions.existsByStudentIdAndCanceledAtIsNull(student.getId())) {
			throw ApiException.conflict("STUDENT_ALREADY_SUBSCRIBED", student.getName() + " já tem mensalidade");
		}
		Subscription s = subscriptions.save(new Subscription(plan.getAcademyId(), student.getId(), terms, today(),
				firstDue));
		cycle.issue(s, today());
		return s;
	}

	private Plan requireLinkable(UUID academyId, UUID planId) {
		Plan plan = plans.require(academyId, planId);
		if (!plan.isActive()) {
			throw ApiException.badRequest("PLAN_INACTIVE", "O plano " + plan.getName() + " está inativo");
		}
		return plan;
	}

	private static Terms toTerms(Plan plan, TermsRequest r) {
		boolean hasType = r.discountType() != null;
		if (hasType != (r.discountValue() != null)
				|| (r.discountType() == DiscountType.PERCENT && r.discountValue() > 100)) {
			throw ApiException.badRequest("INVALID_DISCOUNT", "Desconto inválido");
		}
		int day = r.dueDay() == null ? plan.getDueDay() : r.dueDay();
		String reason = hasType ? TextNormalizer.blankToNull(r.discountReason()) : null;
		return new Terms(plan.getId(), day, r.discountType(), r.discountValue(), reason);
	}

	private LocalDate firstDue(LocalDate requested, int dueDay) {
		LocalDate today = today();
		if (requested == null) {
			return DueDates.nextOccurrence(dueDay, today);
		}
		if (requested.isBefore(today) || requested.isAfter(today.plusMonths(MAX_FIRST_DUE_MONTHS))) {
			throw ApiException.badRequest("INVALID_FIRST_DUE", "O 1º vencimento deve ser entre hoje e 12 meses");
		}
		return requested;
	}

	private SubscriptionView view(Subscription s, Plan plan, Student student) {
		return SubscriptionViews.of(s, plan, student, balances(s.getAcademyId()).get(s.getStudentId()));
	}

	private Map<UUID, OpenBalance> balances(UUID academyId) {
		return invoices.openBalances(academyId, today()).stream()
				.collect(Collectors.toMap(OpenBalance::studentId, Function.identity()));
	}

	private LocalDate today() {
		return LocalDate.now(clock);
	}
}
