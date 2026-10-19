package br.com.academia.api.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.RandomTokens;
import br.com.academia.api.config.AppProperties;
import br.com.academia.api.user.User;
import br.com.academia.api.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pedido de redefinição responde sempre igual, exista ou não o e-mail.
 * O token em claro só vai no e-mail; no banco fica o SHA-256.
 */
@Service
public class PasswordResetService {

	private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
	private static final Duration TTL = Duration.ofHours(1);

	private final UserRepository users;
	private final PasswordResetTokenRepository tokens;
	private final RefreshTokenRepository refreshTokens;
	private final PasswordEncoder passwords;
	private final PasswordResetMail mail;
	private final PasswordResetLimiter limiter;
	private final String publicUrl;

	public PasswordResetService(UserRepository users, PasswordResetTokenRepository tokens,
			RefreshTokenRepository refreshTokens, PasswordEncoder passwords, PasswordResetMail mail,
			PasswordResetLimiter limiter, AppProperties props) {
		this.users = users;
		this.tokens = tokens;
		this.refreshTokens = refreshTokens;
		this.passwords = passwords;
		this.mail = mail;
		this.limiter = limiter;
		this.publicUrl = props.mail().publicUrl().replaceAll("/$", "");
	}

	@Transactional
	public void request(String email, String clientIp) {
		if (!mail.enabled()) {
			throw ApiException.unavailable("RESET_UNAVAILABLE", "A recuperação de senha não está disponível");
		}
		String normalized = email.trim().toLowerCase(Locale.ROOT);
		limiter.consume(clientIp, normalized);
		users.findByEmailIgnoreCase(normalized).ifPresent(this::send);
	}

	@Transactional
	public void reset(String rawToken, String newPassword) {
		PasswordResetToken token = tokens.findByTokenHash(TokenService.sha256(rawToken))
				.filter(found -> found.isUsable(Instant.now()))
				.orElseThrow(() -> ApiException.badRequest("INVALID_RESET_TOKEN", "Este link não é mais válido"));
		User user = token.getUser();
		user.changePassword(passwords.encode(newPassword));
		token.use(Instant.now());
		refreshTokens.deleteAllForUser(user.getId());
	}

	private void send(User user) {
		tokens.invalidateOpen(user.getId(), Instant.now());
		String raw = RandomTokens.urlSafe();
		tokens.save(new PasswordResetToken(user, TokenService.sha256(raw), Instant.now().plus(TTL)));
		try {
			mail.sendResetLink(user.getEmail(), publicUrl + "/redefinir-senha?token=" + raw);
		} catch (RuntimeException ex) {
			log.warn("Falha ao enviar e-mail de redefinição ({})", ex.getClass().getSimpleName());
		}
	}
}
