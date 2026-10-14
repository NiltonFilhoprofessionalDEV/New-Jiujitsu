package br.com.academia.api.billing;

import java.util.UUID;

import br.com.academia.api.billing.InvoiceDtos.InvoiceFilter;
import br.com.academia.api.billing.InvoiceDtos.InvoiceList;
import br.com.academia.api.billing.InvoiceDtos.InvoiceRow;
import br.com.academia.api.billing.InvoiceDtos.PayRequest;
import br.com.academia.api.common.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/invoices")
@PreAuthorize("hasAnyRole('OWNER','CO_MANAGER')")
public class InvoiceController {

	private final InvoiceService service;

	public InvoiceController(InvoiceService service) {
		this.service = service;
	}

	@GetMapping
	public InvoiceList list(
			@RequestParam(required = false) String situation,
			@RequestParam(required = false) String month,
			@RequestParam(required = false) String q,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		return service.list(academyId(), InvoiceFilter.of(situation, month, q), page, size);
	}

	@PostMapping("/{id}/pay")
	public InvoiceRow pay(@PathVariable UUID id, @Valid @RequestBody PayRequest body) {
		CurrentUser user = CurrentUser.get();
		return service.pay(user.academyId(), user.membershipId(), id, body);
	}

	@PostMapping("/{id}/cancel")
	public InvoiceRow cancel(@PathVariable UUID id) {
		return service.cancel(academyId(), id);
	}

	@PostMapping("/{id}/reverse")
	public InvoiceRow reverse(@PathVariable UUID id) {
		return service.reverse(academyId(), id);
	}

	private static UUID academyId() {
		return CurrentUser.get().academyId();
	}
}
