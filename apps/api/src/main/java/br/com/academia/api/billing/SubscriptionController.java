package br.com.academia.api.billing;

import java.util.List;
import java.util.UUID;

import br.com.academia.api.billing.SubscriptionDtos.LinkRequest;
import br.com.academia.api.billing.SubscriptionDtos.SubscriptionView;
import br.com.academia.api.billing.SubscriptionDtos.TermsRequest;
import br.com.academia.api.common.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subscriptions")
@PreAuthorize("hasAnyRole('OWNER','CO_MANAGER')")
public class SubscriptionController {

	private final SubscriptionService service;

	public SubscriptionController(SubscriptionService service) {
		this.service = service;
	}

	@GetMapping
	public List<SubscriptionView> list() {
		return service.list(CurrentUser.get().academyId());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SubscriptionView link(@Valid @RequestBody LinkRequest body) {
		return service.link(CurrentUser.get().academyId(), body);
	}

	@PutMapping("/{id}")
	public SubscriptionView update(@PathVariable UUID id, @Valid @RequestBody TermsRequest body) {
		return service.update(CurrentUser.get().academyId(), id, body);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void cancel(@PathVariable UUID id) {
		service.cancel(CurrentUser.get().academyId(), id);
	}
}
