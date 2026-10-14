package br.com.academia.api.common;

import java.util.UUID;

import br.com.academia.api.membership.MembershipRole;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/** Identidade do token de acesso. A academia ativa vem sempre daqui, nunca do corpo da requisição. */
public record CurrentUser(UUID userId, UUID membershipId, UUID academyId, MembershipRole role) {

	public static CurrentUser get() {
		if (!(SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken auth)) {
			throw ApiException.unauthorized("UNAUTHENTICATED", "Sessão inválida");
		}
		var jwt = auth.getToken();
		return new CurrentUser(
				UUID.fromString(jwt.getSubject()),
				UUID.fromString(jwt.getClaimAsString("mid")),
				UUID.fromString(jwt.getClaimAsString("aid")),
				MembershipRole.valueOf(jwt.getClaimAsString("role")));
	}

	public boolean isManager() {
		return role == MembershipRole.OWNER || role == MembershipRole.CO_MANAGER;
	}
}
