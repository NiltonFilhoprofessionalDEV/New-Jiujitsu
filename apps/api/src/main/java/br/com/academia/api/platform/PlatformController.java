package br.com.academia.api.platform;

import java.util.List;
import java.util.UUID;

import br.com.academia.api.auth.AuthDtos.SessionResponse;
import br.com.academia.api.auth.SessionCookies;
import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.platform.PlatformDtos.AdminAcademy;
import br.com.academia.api.platform.PlatformDtos.DeleteAcademyRequest;
import br.com.academia.api.platform.PlatformDtos.GrantRequest;
import br.com.academia.api.platform.PlatformDtos.PlatformAccess;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Painel do admin da plataforma. A permissão vem do e-mail da conta, não do papel na academia. */
@RestController
@RequestMapping("/platform")
public class PlatformController {

	private final PlatformService platform;
	private final PlatformEnterService enter;
	private final PlatformDeleteService delete;
	private final PlatformAdmins admins;
	private final SessionCookies cookies;

	public PlatformController(PlatformService platform, PlatformEnterService enter, PlatformDeleteService delete,
			PlatformAdmins admins, SessionCookies cookies) {
		this.platform = platform;
		this.enter = enter;
		this.delete = delete;
		this.admins = admins;
		this.cookies = cookies;
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

	@DeleteMapping("/academies/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id, @Valid @RequestBody DeleteAcademyRequest body) {
		delete.delete(CurrentUser.get(), id, body.confirmName());
	}

	@PostMapping("/academies/{id}/enter")
	public ResponseEntity<SessionResponse> enter(@PathVariable UUID id,
			@CookieValue(name = SessionCookies.REFRESH_COOKIE, required = false) String refreshToken) {
		return cookies.respond(HttpStatus.OK, enter.enter(CurrentUser.get(), id, refreshToken));
	}
}
