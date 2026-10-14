package br.com.academia.api.auth;

import br.com.academia.api.auth.AuthDtos.IssuedSession;
import br.com.academia.api.auth.AuthDtos.SessionResponse;
import br.com.academia.api.config.AppProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/** Entrega a sessão: access token no corpo, refresh token só em cookie HttpOnly restrito às rotas de auth. */
@Component
public class SessionCookies {

	static final String REFRESH_COOKIE = "refresh_token";
	private static final String COOKIE_PATH = "/api/v2/auth";

	private final AppProperties props;

	public SessionCookies(AppProperties props) {
		this.props = props;
	}

	public ResponseEntity<SessionResponse> respond(HttpStatus status, IssuedSession session) {
		ResponseCookie cookie = baseCookie(session.refreshToken()).maxAge(props.jwt().refreshTtl()).build();
		return ResponseEntity.status(status).header(HttpHeaders.SET_COOKIE, cookie.toString()).body(session.body());
	}

	ResponseEntity<Void> cleared() {
		ResponseCookie cookie = baseCookie("").maxAge(0).build();
		return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
	}

	private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
		return ResponseCookie.from(REFRESH_COOKIE, value)
				.httpOnly(true)
				.secure(props.cookie().secure())
				.sameSite("Strict")
				.path(COOKIE_PATH);
	}
}
