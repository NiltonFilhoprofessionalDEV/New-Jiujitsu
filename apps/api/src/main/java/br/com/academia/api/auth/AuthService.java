package br.com.academia.api.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import br.com.academia.api.academy.Academy;
import br.com.academia.api.academy.AcademyRepository;
import br.com.academia.api.academy.SlugGenerator;
import br.com.academia.api.auth.AuthDtos.IssuedSession;
import br.com.academia.api.auth.AuthDtos.LoginRequest;
import br.com.academia.api.auth.AuthDtos.MembershipSummary;
import br.com.academia.api.auth.AuthDtos.RegisterAcademyRequest;
import br.com.academia.api.auth.AuthDtos.SessionResponse;
import br.com.academia.api.auth.AuthDtos.UserSummary;
import br.com.academia.api.common.ApiException;
import br.com.academia.api.membership.Membership;
import br.com.academia.api.membership.MembershipRepository;
import br.com.academia.api.membership.MembershipRole;
import br.com.academia.api.user.User;
import br.com.academia.api.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private static final Duration TRIAL = Duration.ofDays(14);

	private final UserRepository users;
	private final AcademyRepository academies;
	private final MembershipRepository memberships;
	private final SlugGenerator slugs;
	private final PasswordEncoder passwords;
	private final TokenService tokens;
	private final LoginRateLimiter rateLimiter;
	private final String timingDummyHash;

	public AuthService(UserRepository users, AcademyRepository academies, MembershipRepository memberships,
			SlugGenerator slugs, PasswordEncoder passwords, TokenService tokens, LoginRateLimiter rateLimiter) {
		this.users = users;
		this.academies = academies;
		this.memberships = memberships;
		this.slugs = slugs;
		this.passwords = passwords;
		this.tokens = tokens;
		this.rateLimiter = rateLimiter;
		this.timingDummyHash = passwords.encode(UUID.randomUUID().toString());
	}

	@Transactional
	public IssuedSession registerAcademy(RegisterAcademyRequest req) {
		User user = createUser(req.name(), req.email(), req.password());
		String name = req.academyName().trim();
		Academy academy = academies.save(new Academy(name, slugs.uniqueSlug(name), Instant.now().plus(TRIAL)));
		Membership owner = memberships.save(new Membership(user, academy, MembershipRole.OWNER));
		return issue(owner);
	}

	/** Cria a conta de login. O e-mail é único no sistema todo, não por academia. */
	@Transactional
	public User createUser(String name, String email, String rawPassword) {
		String normalized = normalizeEmail(email);
		if (users.existsByEmailIgnoreCase(normalized)) {
			throw ApiException.conflict("EMAIL_IN_USE", "Este e-mail já tem cadastro. Entre com sua senha.");
		}
		return users.save(new User(normalized, passwords.encode(rawPassword), name.trim()));
	}

	/** Abre uma sessão para um vínculo já validado (ex.: convite aceito). */
	@Transactional
	public IssuedSession startSession(Membership membership) {
		return issue(membership);
	}

	@Transactional
	public IssuedSession login(LoginRequest req, String clientIp) {
		String email = normalizeEmail(req.email());
		String limiterKey = clientIp + "|" + email;
		rateLimiter.check(limiterKey);

		User user = users.findByEmailIgnoreCase(email).orElse(null);
		String hash = user != null ? user.getPasswordHash() : timingDummyHash;
		if (!passwords.matches(req.password(), hash) || user == null) {
			rateLimiter.recordFailure(limiterKey);
			throw ApiException.unauthorized("INVALID_CREDENTIALS", "E-mail ou senha incorretos");
		}
		rateLimiter.reset(limiterKey);

		List<Membership> active = memberships.findActiveByUser(user.getId());
		if (active.isEmpty()) {
			throw ApiException.forbidden("NO_ACTIVE_MEMBERSHIP", "Seu acesso ainda não foi liberado pela academia");
		}
		return issue(active.getFirst());
	}

	@Transactional(noRollbackFor = ApiException.class)
	public IssuedSession refresh(String refreshToken) {
		return issue(tokens.consumeRefreshToken(refreshToken));
	}

	@Transactional
	public IssuedSession switchAcademy(UUID userId, UUID membershipId, String currentRefreshToken) {
		Membership target = memberships.findActiveById(membershipId)
				.filter(m -> m.getUser().getId().equals(userId))
				.orElseThrow(() -> ApiException.forbidden("MEMBERSHIP_NOT_ALLOWED", "Academia não disponível para esta conta"));
		if (currentRefreshToken != null) {
			tokens.revoke(currentRefreshToken);
		}
		return issue(target);
	}

	public void logout(String refreshToken) {
		if (refreshToken != null) {
			tokens.revoke(refreshToken);
		}
	}

	private IssuedSession issue(Membership active) {
		active.markUsed();
		User user = active.getUser();
		List<MembershipSummary> all = memberships.findActiveByUser(user.getId()).stream()
				.map(MembershipSummary::of).toList();
		SessionResponse body = new SessionResponse(
				tokens.issueAccessToken(active),
				tokens.accessTtlSeconds(),
				new UserSummary(user.getId(), user.getName(), user.getEmail()),
				active.getId(),
				all);
		return new IssuedSession(body, tokens.createRefreshToken(active));
	}

	private static String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
