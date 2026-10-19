package br.com.academia.api.auth;

import java.time.Duration;

import br.com.academia.api.common.SlidingWindowLimiter;
import org.springframework.stereotype.Component;

/** 5 pedidos por hora, por IP e por e-mail, para o fluxo de esqueci a senha. */
@Component
public class PasswordResetLimiter {

	private static final int MAX = 5;

	private final SlidingWindowLimiter attempts = new SlidingWindowLimiter(MAX, Duration.ofHours(1));

	public void consume(String clientIp, String email) {
		attempts.check("ip|" + clientIp);
		attempts.check("email|" + email);
		attempts.record("ip|" + clientIp);
		attempts.record("email|" + email);
	}
}
