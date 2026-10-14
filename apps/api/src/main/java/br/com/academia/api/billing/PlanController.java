package br.com.academia.api.billing;

import java.util.List;
import java.util.UUID;

import br.com.academia.api.billing.PlanDtos.PlanRequest;
import br.com.academia.api.billing.PlanDtos.PlanView;
import br.com.academia.api.common.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Financeiro é só da gestão: o professor não vê valores. */
@RestController
@RequestMapping("/plans")
@PreAuthorize("hasAnyRole('OWNER','CO_MANAGER')")
public class PlanController {

	private final PlanService service;

	public PlanController(PlanService service) {
		this.service = service;
	}

	@GetMapping
	public List<PlanView> list() {
		return service.list(CurrentUser.get().academyId());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PlanView create(@Valid @RequestBody PlanRequest body) {
		return service.create(CurrentUser.get().academyId(), body);
	}

	@PutMapping("/{id}")
	public PlanView update(@PathVariable UUID id, @Valid @RequestBody PlanRequest body) {
		return service.update(CurrentUser.get().academyId(), id, body);
	}
}
