package br.com.academia.api.access;

import java.util.List;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.membership.StaffService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Bloqueia a equipe de academia suspensa (teste ou pagamento vencidos além da tolerância).
 * Alunos e responsáveis nunca são bloqueados: a cobrança é entre a academia e a plataforma.
 */
@Component
public class SuspendedAcademyGuard implements HandlerInterceptor {

	/** O que a equipe bloqueada ainda precisa para sair do bloqueio: sessão, assinatura e pagamento. */
	private static final List<String> ALWAYS_OPEN = List.of(
			"/me", "/auth", "/academy-access", "/platform", "/public", "/webhooks", "/actuator");

	private final AcademyAccessService access;

	public SuspendedAcademyGuard(AcademyAccessService access) {
		this.access = access;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (!access.enforced() || !(SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken)) {
			return true;
		}
		String path = request.getRequestURI().substring(request.getContextPath().length());
		if (ALWAYS_OPEN.stream().anyMatch(open -> path.equals(open) || path.startsWith(open + "/"))) {
			return true;
		}
		CurrentUser me = CurrentUser.get();
		if (StaffService.STAFF_ROLES.contains(me.role())
				&& access.accessOf(me.academyId()).status() == AccessStatus.SUSPENDED) {
			throw ApiException.forbidden("ACADEMY_SUSPENDED",
					"O acesso da academia está suspenso. Regularize a assinatura para continuar.");
		}
		return true;
	}
}
