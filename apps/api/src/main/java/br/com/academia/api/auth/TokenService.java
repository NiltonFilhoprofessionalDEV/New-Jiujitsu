package br.com.academia.api.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.RandomTokens;
import br.com.academia.api.config.AppProperties;
import br.com.academia.api.membership.Membership;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenService {

	private final JwtEncoder encoder;
	private final RefreshTokenRepository refreshTokens;
	private final AppProperties.Jwt jwt;

	public TokenService(JwtEncoder encoder, RefreshTokenRepository refreshTokens, AppProperties props) {
		this.encoder = encoder;
		this.refreshTokens = refreshTokens;
		this.jwt = props.jwt();
	}

	public String issueAccessToken(Membership membership) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(jwt.issuer())
				.issuedAt(now)
				.expiresAt(now.plus(jwt.accessTtl()))
				.subject(membership.getUser().getId().toString())
				.claim("mid", membership.getId().toString())
				.claim("aid", membership.getAcademy().getId().toString())
				.claim("role", membership.getRole().name())
				.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}

	public long accessTtlSeconds() {
		return jwt.accessTtl().toSeconds();
	}

	@Transactional
	public String createRefreshToken(Membership membership) {
		String raw = RandomTokens.urlSafe();
		refreshTokens.save(new RefreshToken(membership, sha256(raw), Instant.now().plus(jwt.refreshTtl())));
		return raw;
	}

	/** Consome o refresh token e devolve o vínculo dono dele. Reuso de token revogado derruba todas as sessões. */
	@Transactional(noRollbackFor = ApiException.class)
	public Membership consumeRefreshToken(String raw) {
		RefreshToken token = refreshTokens.findByTokenHash(sha256(raw))
				.orElseThrow(TokenService::invalidSession);
		if (token.isRevoked()) {
			refreshTokens.revokeAllForUser(token.getUserId());
			throw invalidSession();
		}
		if (!token.isUsable(Instant.now()) || !token.getMembership().isActive()) {
			throw invalidSession();
		}
		token.revoke();
		return token.getMembership();
	}

	@Transactional
	public void revoke(String raw) {
		refreshTokens.findByTokenHash(sha256(raw)).ifPresent(RefreshToken::revoke);
	}

	static String sha256(String value) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	private static ApiException invalidSession() {
		return ApiException.unauthorized("INVALID_SESSION", "Sessão expirada. Entre novamente.");
	}
}
