package br.com.academia.api.access;

import br.com.academia.api.access.AcademyAccessDtos.AccessView;
import br.com.academia.api.access.AcademyAccessDtos.CheckoutRequest;
import br.com.academia.api.access.AcademyAccessDtos.CheckoutResponse;
import br.com.academia.api.common.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Assinatura da academia na plataforma (não confundir com as mensalidades dos alunos). */
@RestController
@RequestMapping("/academy-access")
public class AcademyAccessController {

	private final AcademyAccessService access;
	private final CheckoutService checkout;

	public AcademyAccessController(AcademyAccessService access, CheckoutService checkout) {
		this.access = access;
		this.checkout = checkout;
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('OWNER','CO_MANAGER','INSTRUCTOR')")
	public AccessView current() {
		return access.view(CurrentUser.get().academyId());
	}

	/** Devolve a página de pagamento da fatura em aberto, assinando antes se for a primeira vez. */
	@PostMapping("/checkout")
	@PreAuthorize("hasAnyRole('OWNER','CO_MANAGER')")
	public CheckoutResponse checkout(@Valid @RequestBody(required = false) CheckoutRequest body) {
		return checkout.checkout(CurrentUser.get(), body);
	}
}
