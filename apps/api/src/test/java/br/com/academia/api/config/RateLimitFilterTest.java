package br.com.academia.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import br.com.academia.api.common.FixedWindowCounter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitFilterTest {

	static final int LIMIT = 3;

	/** Relógio que o teste avança à mão. */
	static final class ManualClock extends Clock {

		private Instant now = Instant.parse("2026-10-14T12:00:10Z");

		void advance(Duration d) {
			now = now.plus(d);
		}

		@Override
		public ZoneId getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return now;
		}
	}

	final ManualClock clock = new ManualClock();
	final RateLimitFilter filter = new RateLimitFilter(new FixedWindowCounter(LIMIT, Duration.ofMinutes(1), clock));

	@Test
	void blocksAfterLimitPerIpUntilNextWindow() throws Exception {
		for (int i = 0; i < LIMIT; i++) {
			assertThat(call("10.0.0.1", "/students").getStatus()).isEqualTo(200);
		}
		MockHttpServletResponse blocked = call("10.0.0.1", "/students");
		assertThat(blocked.getStatus()).isEqualTo(429);
		assertThat(blocked.getHeader("Retry-After")).isEqualTo("50");
		assertThat(blocked.getContentAsString()).contains("\"code\":\"TOO_MANY_REQUESTS\"");

		assertThat(call("10.0.0.2", "/students").getStatus()).as("outro IP tem a própria cota").isEqualTo(200);

		clock.advance(Duration.ofSeconds(50));
		assertThat(call("10.0.0.1", "/students").getStatus()).isEqualTo(200);
	}

	@Test
	void healthCheckIsNeverLimited() throws Exception {
		for (int i = 0; i < LIMIT * 2; i++) {
			assertThat(call("10.0.0.1", "/actuator/health").getStatus()).isEqualTo(200);
		}
	}

	private MockHttpServletResponse call(String ip, String path) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v2" + path);
		request.setContextPath("/api/v2");
		request.setServletPath(path);
		request.setRemoteAddr(ip);
		MockHttpServletResponse response = new MockHttpServletResponse();
		filter.doFilter(request, response, new MockFilterChain());
		return response;
	}
}
