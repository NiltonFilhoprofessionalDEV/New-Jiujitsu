package br.com.academia.api.config;

import java.time.Clock;
import java.time.Duration;

import br.com.academia.api.common.FixedWindowCounter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class RateLimitConfig {

	private static final Duration WINDOW = Duration.ofMinutes(1);
	/** Depois do {@code ForwardedHeaderFilter} (que troca o IP pelo do cliente) e antes do Spring Security. */
	private static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 10;

	@Bean
	FilterRegistrationBean<RateLimitFilter> rateLimitFilter(AppProperties props) {
		FixedWindowCounter counter = new FixedWindowCounter(props.rateLimit().requestsPerMinute(), WINDOW,
				Clock.systemUTC());
		FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>(new RateLimitFilter(counter));
		registration.setOrder(ORDER);
		return registration;
	}
}
