package br.com.academia.api.auth;

import br.com.academia.api.auth.AuthDtos.ChangePasswordRequest;
import br.com.academia.api.auth.AuthDtos.IssuedSession;
import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.membership.Membership;
import br.com.academia.api.membership.MembershipRepository;
import br.com.academia.api.user.User;
import br.com.academia.api.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Troca de senha do usuário logado. Exige a senha atual (um access token vazado não basta) e derruba as
 * outras sessões; este dispositivo recebe uma sessão nova.
 */
@Service
public class PasswordService {

	private static final String LIMITER_PREFIX = "password|";

	private final UserRepository users;
	private final MembershipRepository memberships;
	private final RefreshTokenRepository refreshTokens;
	private final PasswordEncoder passwords;
	private final LoginRateLimiter rateLimiter;
	private final AuthService auth;

	public PasswordService(UserRepository users, MembershipRepository memberships,
			RefreshTokenRepository refreshTokens, PasswordEncoder passwords, LoginRateLimiter rateLimiter,
			AuthService auth) {
		this.users = users;
		this.memberships = memberships;
		this.refreshTokens = refreshTokens;
		this.passwords = passwords;
		this.rateLimiter = rateLimiter;
		this.auth = auth;
	}

	@Transactional
	public IssuedSession change(CurrentUser me, ChangePasswordRequest req) {
		String limiterKey = LIMITER_PREFIX + me.userId();
		rateLimiter.check(limiterKey);
		User user = users.findById(me.userId()).orElseThrow(PasswordService::invalidSession);
		if (!passwords.matches(req.currentPassword(), user.getPasswordHash())) {
			rateLimiter.recordFailure(limiterKey);
			throw ApiException.badRequest("WRONG_PASSWORD", "A senha atual não confere");
		}
		rateLimiter.reset(limiterKey);
		if (passwords.matches(req.newPassword(), user.getPasswordHash())) {
			throw ApiException.badRequest("PASSWORD_UNCHANGED", "A nova senha precisa ser diferente da atual");
		}
		Membership current = memberships.findActiveById(me.membershipId())
				.orElseThrow(PasswordService::invalidSession);

		user.changePassword(passwords.encode(req.newPassword()));
		refreshTokens.deleteAllForUser(user.getId());
		return auth.startSession(current);
	}

	private static ApiException invalidSession() {
		return ApiException.unauthorized("INVALID_SESSION", "Sessão expirada. Entre novamente.");
	}
}
