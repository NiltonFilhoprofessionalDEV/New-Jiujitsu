package br.com.academia.api.invite;

import br.com.academia.api.common.SlidingWindowLimiter;
import br.com.academia.api.config.AppProperties;
import org.springframework.stereotype.Component;

/** Limita tentativas de cadastro público por IP, com ou sem sucesso. */
@Component
public class SignupRateLimiter {

	private final SlidingWindowLimiter attempts;

	public SignupRateLimiter(AppProperties props) {
		this.attempts = new SlidingWindowLimiter(props.signup().maxAttempts(), props.signup().window());
	}

	void consume(String clientIp) {
		attempts.check(clientIp);
		attempts.record(clientIp);
	}
}
