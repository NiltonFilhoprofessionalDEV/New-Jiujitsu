package br.com.academia.api.config;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
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
		@NotNull ZoneId timeZone,
		@NotNull @Valid Subscription subscription,
		@NotNull Platform platform,
		@NotNull @Valid Asaas asaas) {

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

	/**
	 * Assinatura das academias na plataforma. Com {@code enforced} desligado, o app fica livre:
	 * ninguém é bloqueado e a tela de assinatura some.
	 */
	public record Subscription(
			boolean enforced,
			@Min(1) int trialDays,
			@Min(0) int graceDays,
			@NotNull @DecimalMin("1.00") BigDecimal monthlyPrice) {

		public Duration trial() {
			return Duration.ofDays(trialDays);
		}

		public Duration grace() {
			return Duration.ofDays(graceDays);
		}
	}

	/** E-mails dos administradores da plataforma (painel {@code /admin}). */
	public record Platform(List<String> adminEmails) {

		public List<String> adminEmails() {
			return adminEmails == null ? List.of()
					: adminEmails.stream().map(s -> s.trim().toLowerCase(Locale.ROOT)).filter(s -> !s.isEmpty()).toList();
		}
	}

	/** Integração com o Asaas. Sem chave, o pagamento online fica desligado. */
	public record Asaas(@NotBlank String baseUrl, String apiKey, String webhookToken) {

		public boolean enabled() {
			return apiKey != null && !apiKey.isBlank();
		}

		@Override
		public String toString() {
			return "Asaas[baseUrl=" + baseUrl + ", enabled=" + enabled() + "]";
		}
	}
}
