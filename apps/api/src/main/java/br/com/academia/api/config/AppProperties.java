package br.com.academia.api.config;

import java.time.Duration;
import java.time.ZoneId;
import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
		@NotNull Jwt jwt,
		@NotNull Cookie cookie,
		@NotNull Cors cors,
		@NotNull Login login,
		@NotNull Signup signup,
		@NotNull RateLimit rateLimit,
		@NotNull ZoneId timeZone) {

	public record Jwt(
			@NotBlank String secret,
			@NotBlank String issuer,
			@NotNull Duration accessTtl,
			@NotNull Duration refreshTtl) {
	}

	public record Cookie(boolean secure) {
	}

	public record Cors(List<String> allowedOrigins) {

		public List<String> allowedOrigins() {
			return allowedOrigins == null ? List.of()
					: allowedOrigins.stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
		}
	}

	public record Login(@Min(1) int maxAttempts, @NotNull Duration window) {
	}

	/** Cadastros públicos (link de matrícula e convites) por IP, contando sucessos e falhas. */
	public record Signup(@Min(1) int maxAttempts, @NotNull Duration window) {
	}

	/** Teto geral de requisições por IP, por minuto. */
	public record RateLimit(@Min(1) int requestsPerMinute) {
	}
}
