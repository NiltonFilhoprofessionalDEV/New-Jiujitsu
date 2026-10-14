package br.com.academia.api.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import br.com.academia.api.common.FixedWindowCounter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Teto de requisições por IP para a API inteira, antes da autenticação. Os limites finos
 * (login, cadastro, senha) continuam nos serviços. O IP real depende de
 * {@code server.forward-headers-strategy} quando há proxy na frente.
 */
public class RateLimitFilter extends OncePerRequestFilter {

	private static final String HEALTH_PATH = "/actuator/health";
	private static final String BODY = """
			{"type":"about:blank","title":"Too Many Requests","status":429,\
			"detail":"Muitas requisições. Aguarde um instante e tente de novo.","code":"TOO_MANY_REQUESTS"}""";

	private final FixedWindowCounter counter;

	public RateLimitFilter(FixedWindowCounter counter) {
		this.counter = counter;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return HEALTH_PATH.equals(request.getServletPath());
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		if (counter.tryAcquire(request.getRemoteAddr())) {
			chain.doFilter(request, response);
			return;
		}
		response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
		response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(counter.secondsUntilReset()));
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.getWriter().write(BODY);
	}
}
