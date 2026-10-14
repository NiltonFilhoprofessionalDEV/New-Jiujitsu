package br.com.academia.api.billing;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.academia.api.billing.PlanDtos.PlanRequest;
import br.com.academia.api.billing.PlanDtos.PlanView;
import br.com.academia.api.common.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Planos não são excluídos: um plano em uso é desativado e segue cobrando quem já está nele. */
@Service
public class PlanService {

	private final PlanRepository plans;
	private final SubscriptionRepository subscriptions;

	public PlanService(PlanRepository plans, SubscriptionRepository subscriptions) {
		this.plans = plans;
		this.subscriptions = subscriptions;
	}

	@Transactional(readOnly = true)
	public List<PlanView> list(UUID academyId) {
		Map<UUID, Long> counts = subscriptions.countActiveByPlan(academyId).stream()
				.collect(Collectors.toMap(PlanCount::planId, PlanCount::count));
		return plans.findByAcademyIdOrderByNameAsc(academyId).stream()
				.map(p -> PlanView.of(p, counts.getOrDefault(p.getId(), 0L)))
				.toList();
	}

	@Transactional
	public PlanView create(UUID academyId, PlanRequest request) {
		requireDefaultsActive(request);
		Plan plan = new Plan(academyId);
		plan.apply(request);
		plans.save(plan);
		applyDefaults(plan, request);
		return PlanView.of(plan, 0);
	}

	@Transactional
	public PlanView update(UUID academyId, UUID id, PlanRequest request) {
		requireDefaultsActive(request);
		Plan plan = require(academyId, id);
		plan.apply(request);
		applyDefaults(plan, request);
		long subscribers = subscriptions.countActiveByPlan(academyId).stream()
				.filter(c -> c.planId().equals(id)).mapToLong(PlanCount::count).sum();
		return PlanView.of(plan, subscribers);
	}

	Plan require(UUID academyId, UUID id) {
		return plans.findByIdAndAcademyId(id, academyId)
				.orElseThrow(() -> ApiException.notFound("PLAN_NOT_FOUND", "Plano não encontrado"));
	}

	private static void requireDefaultsActive(PlanRequest r) {
		if (!r.active() && (r.isDefault() || r.isKidsDefault())) {
			throw ApiException.badRequest("INVALID_PLAN_DEFAULT", "Um plano padrão precisa estar ativo");
		}
	}

	/** Desmarca o padrão anterior antes de marcar este: o índice único parcial não admite dois. */
	private void applyDefaults(Plan plan, PlanRequest r) {
		if (r.isDefault() && !plan.isDefault()) {
			plans.clearDefault(plan.getAcademyId());
		}
		if (r.isKidsDefault() && !plan.isKidsDefault()) {
			plans.clearKidsDefault(plan.getAcademyId());
		}
		plan.setDefault(r.isDefault());
		plan.setKidsDefault(r.isKidsDefault());
	}
}
