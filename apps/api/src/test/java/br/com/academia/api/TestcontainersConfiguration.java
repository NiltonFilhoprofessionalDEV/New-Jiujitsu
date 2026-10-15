package br.com.academia.api;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	public static final String PLATFORM_ADMIN_EMAIL = "admin.plataforma@teste.dev";
	public static final String ASAAS_WEBHOOK_TOKEN = "token-de-teste-do-webhook-asaas-0123456789";

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgresContainer() {
		return new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));
	}

	@Bean
	DynamicPropertyRegistrar testSecrets() {
		byte[] secret = new byte[48];
		new SecureRandom().nextBytes(secret);
		String encoded = Base64.getEncoder().encodeToString(secret);
		return registry -> {
			registry.add("app.jwt.secret", () -> encoded);
			registry.add("app.cookie.secure", () -> "true");
			// Toda a suíte sai do mesmo IP; o teto geral tem teste próprio
			registry.add("app.rate-limit.requests-per-minute", () -> "1000000");
			registry.add("app.subscription.enforced", () -> "true");
			registry.add("app.platform.admin-emails", () -> PLATFORM_ADMIN_EMAIL);
			registry.add("app.asaas.api-key", () -> "chave-de-teste");
			registry.add("app.asaas.webhook-token", () -> ASAAS_WEBHOOK_TOKEN);
		};
	}

}
