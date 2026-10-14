package br.com.academia.api.graduation;

import java.util.List;

import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.graduation.BeltRuleDtos.RuleView;
import br.com.academia.api.graduation.BeltRuleDtos.SaveRulesRequest;
import br.com.academia.api.graduation.GraduationDtos.ReadyStudent;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Aba "Faixas": alunos prontos para graduar e regras por faixa. */
@RestController
@RequestMapping("/graduations")
@PreAuthorize("hasAnyRole('OWNER','CO_MANAGER','INSTRUCTOR')")
public class BeltOverviewController {

	private final ReadinessService readiness;
	private final BeltRuleService rules;

	public BeltOverviewController(ReadinessService readiness, BeltRuleService rules) {
		this.readiness = readiness;
		this.rules = rules;
	}

	@GetMapping("/ready")
	public List<ReadyStudent> ready() {
		return readiness.ready(CurrentUser.get().academyId());
	}

	@GetMapping("/rules")
	public List<RuleView> rules() {
		return rules.list(CurrentUser.get().academyId());
	}

	@PutMapping("/rules")
	@PreAuthorize("hasAnyRole('OWNER','CO_MANAGER')")
	public List<RuleView> saveRules(@Valid @RequestBody SaveRulesRequest body) {
		return rules.save(CurrentUser.get().academyId(), body.rules());
	}
}
