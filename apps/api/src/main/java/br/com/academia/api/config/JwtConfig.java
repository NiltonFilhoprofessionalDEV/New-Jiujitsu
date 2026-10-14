package br.com.academia.api.config;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtConfig {

	private static final int MIN_SECRET_BYTES = 32;

	private final SecretKey key;
	private final String issuer;

	public JwtConfig(AppProperties props) {
		byte[] secret = Base64.getDecoder().decode(props.jwt().secret());
		if (secret.length < MIN_SECRET_BYTES) {
			throw new IllegalStateException("APP_JWT_SECRET deve ter pelo menos 32 bytes em base64");
		}
		this.key = new SecretKeySpec(secret, "HmacSHA256");
		this.issuer = props.jwt().issuer();
	}

	@Bean
	JwtEncoder jwtEncoder() {
		return new NimbusJwtEncoder(new ImmutableSecret<>(key));
	}

	@Bean
	JwtDecoder jwtDecoder() {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
		decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
		return decoder;
	}
}
