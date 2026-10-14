package br.com.academia.api.auth;

import br.com.academia.api.auth.AuthDtos.ChangePasswordRequest;
import br.com.academia.api.auth.AuthDtos.LoginRequest;
import br.com.academia.api.auth.AuthDtos.RegisterAcademyRequest;
import br.com.academia.api.auth.AuthDtos.SessionResponse;
import br.com.academia.api.auth.AuthDtos.SwitchAcademyRequest;
import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

	static final String REFRESH_COOKIE = SessionCookies.REFRESH_COOKIE;

	private final AuthService auth;
	private final PasswordService passwords;
	private final SessionCookies cookies;

	public AuthController(AuthService auth, PasswordService passwords, SessionCookies cookies) {
		this.auth = auth;
		this.passwords = passwords;
		this.cookies = cookies;
	}

	@PostMapping("/register-academy")
	public ResponseEntity<SessionResponse> registerAcademy(@Valid @RequestBody RegisterAcademyRequest body) {
		return cookies.respond(HttpStatus.CREATED, auth.registerAcademy(body));
	}

	@PostMapping("/login")
	public ResponseEntity<SessionResponse> login(@Valid @RequestBody LoginRequest body, HttpServletRequest request) {
		return cookies.respond(HttpStatus.OK, auth.login(body, request.getRemoteAddr()));
	}

	@PostMapping("/refresh")
	public ResponseEntity<SessionResponse> refresh(
			@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank()) {
			throw ApiException.unauthorized("INVALID_SESSION", "Sessão expirada. Entre novamente.");
		}
		return cookies.respond(HttpStatus.OK, auth.refresh(refreshToken));
	}

	@PostMapping("/switch-academy")
	public ResponseEntity<SessionResponse> switchAcademy(@Valid @RequestBody SwitchAcademyRequest body,
			@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
		CurrentUser me = CurrentUser.get();
		return cookies.respond(HttpStatus.OK, auth.switchAcademy(me.userId(), body.membershipId(), refreshToken));
	}

	@PostMapping("/password")
	public ResponseEntity<SessionResponse> changePassword(@Valid @RequestBody ChangePasswordRequest body) {
		return cookies.respond(HttpStatus.OK, passwords.change(CurrentUser.get(), body));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
		auth.logout(refreshToken);
		return cookies.cleared();
	}
}
