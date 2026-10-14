package br.com.academia.api.team;

import br.com.academia.api.auth.AuthDtos.SessionResponse;
import br.com.academia.api.auth.SessionCookies;
import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.team.TeamDtos.PublicStaffInviteInfo;
import br.com.academia.api.team.TeamDtos.StaffSignupRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Aceite do convite de equipe: público (conta nova) ou logado (conta existente). Ambos abrem a sessão nova. */
@RestController
public class StaffInviteAcceptController {

	private static final int MAX_TOKEN = 64;

	private final StaffInviteService service;
	private final SessionCookies cookies;

	public StaffInviteAcceptController(StaffInviteService service, SessionCookies cookies) {
		this.service = service;
		this.cookies = cookies;
	}

	@GetMapping("/public/staff-invites/{token}")
	public PublicStaffInviteInfo info(@PathVariable @Size(max = MAX_TOKEN) String token) {
		return service.publicInfo(token);
	}

	@PostMapping("/public/staff-invites/{token}/accept")
	public ResponseEntity<SessionResponse> acceptNew(@PathVariable @Size(max = MAX_TOKEN) String token,
			@Valid @RequestBody StaffSignupRequest body, HttpServletRequest request) {
		return cookies.respond(HttpStatus.CREATED,
				service.acceptWithNewAccount(token, body, request.getRemoteAddr()));
	}

	@PostMapping("/staff-invites/{token}/accept")
	public ResponseEntity<SessionResponse> acceptExisting(@PathVariable @Size(max = MAX_TOKEN) String token) {
		return cookies.respond(HttpStatus.CREATED,
				service.acceptWithExistingAccount(token, CurrentUser.get().userId()));
	}
}
