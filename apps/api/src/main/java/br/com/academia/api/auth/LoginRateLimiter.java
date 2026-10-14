package br.com.academia.api.auth;

import br.com.academia.api.common.SlidingWindowLimiter;
import br.com.academia.api.config.AppProperties;
import org.springframework.stereotype.Component;

/** Limite de falhas de login por chave (IP + e-mail). */
@Component
public class LoginRateLimiter {

	private final SlidingWindowLimiter failures;

	public LoginRateLimiter(AppProperties props) {
		this.failures = new SlidingWindowLimiter(props.login().maxAttempts(), props.login().window());
	}

	public void check(String key) {
		failures.check(key);
	}

	public void recordFailure(String key) {
		failures.record(key);
	}

	public void reset(String key) {
		failures.reset(key);
	}
}
