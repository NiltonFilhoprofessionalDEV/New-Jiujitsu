package br.com.academia.api.platform;

import java.util.List;
import java.util.UUID;

import br.com.academia.api.auth.AuthDtos.RegisterAcademyRequest;
import br.com.academia.api.auth.AuthDtos.SessionResponse;
import br.com.academia.api.auth.SessionCookies;
import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.platform.AcademyInviteDtos.AcademyInviteView;
import br.com.academia.api.platform.AcademyInviteDtos.PublicAcademyInviteInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Convite para criar academia: o admin gera o link; o aceite público abre a academia e a sessão. */
@RestController
public class AcademyInviteController {

	private static final int MAX_TOKEN = 64;

	private final AcademyInviteService invites;
	private final SessionCookies cookies;

	public AcademyInviteController(AcademyInviteService invites, SessionCookies cookies) {
		this.invites = invites;
		this.cookies = cookies;
	}

	@GetMapping("/platform/academy-invites")
	public List<AcademyInviteView> pending() {
		return invites.pending(CurrentUser.get());
	}

	@PostMapping("/platform/academy-invites")
	@ResponseStatus(HttpStatus.CREATED)
	public AcademyInviteView create() {
		return invites.create(CurrentUser.get());
	}

	@DeleteMapping("/platform/academy-invites/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void revoke(@PathVariable UUID id) {
		invites.revoke(CurrentUser.get(), id);
	}

	@GetMapping("/public/academy-invites/{token}")
	public PublicAcademyInviteInfo info(@PathVariable @Size(max = MAX_TOKEN) String token) {
		return invites.publicInfo(token);
	}

	@PostMapping("/public/academy-invites/{token}/accept")
	public ResponseEntity<SessionResponse> accept(@PathVariable @Size(max = MAX_TOKEN) String token,
			@Valid @RequestBody RegisterAcademyRequest body, HttpServletRequest request) {
		return cookies.respond(HttpStatus.CREATED, invites.accept(token, body, request.getRemoteAddr()));
	}
}
