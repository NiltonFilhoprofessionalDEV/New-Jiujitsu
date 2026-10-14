package br.com.academia.api.invite;

import br.com.academia.api.auth.AuthDtos.SessionResponse;
import br.com.academia.api.auth.SessionCookies;
import br.com.academia.api.invite.InviteDtos.PublicInviteInfo;
import br.com.academia.api.invite.InviteDtos.PublicLinkInfo;
import br.com.academia.api.invite.InviteDtos.SignupRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Rotas sem login. Os tokens têm 256 bits: adivinhar um é inviável, então só o cadastro é limitado por IP. */
@RestController
@RequestMapping("/public")
public class PublicSignupController {

	private static final int MAX_TOKEN = 64;

	private final PublicSignupService service;
	private final SessionCookies cookies;

	public PublicSignupController(PublicSignupService service, SessionCookies cookies) {
		this.service = service;
		this.cookies = cookies;
	}

	@GetMapping("/enroll/{token}")
	public PublicLinkInfo linkInfo(@PathVariable @Size(max = MAX_TOKEN) String token) {
		return service.linkInfo(token);
	}

	@PostMapping("/enroll/{token}")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void enroll(@PathVariable @Size(max = MAX_TOKEN) String token, @Valid @RequestBody SignupRequest body,
			HttpServletRequest request) {
		service.enroll(token, body, request.getRemoteAddr());
	}

	@GetMapping("/invites/{token}")
	public PublicInviteInfo inviteInfo(@PathVariable @Size(max = MAX_TOKEN) String token) {
		return service.inviteInfo(token);
	}

	@PostMapping("/invites/{token}/accept")
	public ResponseEntity<SessionResponse> accept(@PathVariable @Size(max = MAX_TOKEN) String token,
			@Valid @RequestBody SignupRequest body, HttpServletRequest request) {
		return cookies.respond(HttpStatus.CREATED, service.acceptInvite(token, body, request.getRemoteAddr()));
	}
}
