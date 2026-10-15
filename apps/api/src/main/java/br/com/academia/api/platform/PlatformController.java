package br.com.academia.api.platform;

import java.util.List;
import java.util.UUID;

import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.platform.PlatformDtos.AdminAcademy;
import br.com.academia.api.platform.PlatformDtos.GrantRequest;
import br.com.academia.api.platform.PlatformDtos.PlatformAccess;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Painel do admin da plataforma. A permissão vem do e-mail da conta, não do papel na academia. */
@RestController
@RequestMapping("/platform")
public class PlatformController {

	private final PlatformService platform;
	private final PlatformAdmins admins;

	public PlatformController(PlatformService platform, PlatformAdmins admins) {
		this.platform = platform;
		this.admins = admins;
	}

	@GetMapping("/access")
	public PlatformAccess access() {
		return new PlatformAccess(admins.isAdmin(CurrentUser.get().userId()));
	}

	@GetMapping("/academies")
	public List<AdminAcademy> academies() {
		return platform.academies(CurrentUser.get());
	}

	@PostMapping("/academies/{id}/grants")
	public AdminAcademy grant(@PathVariable UUID id, @Valid @RequestBody GrantRequest body) {
		return platform.grant(CurrentUser.get(), id, body);
	}
}
