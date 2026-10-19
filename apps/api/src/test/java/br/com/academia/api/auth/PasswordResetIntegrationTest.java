package br.com.academia.api.auth;

import static br.com.academia.api.ApiTestSupport.CTX;
import static br.com.academia.api.ApiTestSupport.PASSWORD;
import static br.com.academia.api.ApiTestSupport.json;
import static br.com.academia.api.ApiTestSupport.registerBody;
import static br.com.academia.api.ApiTestSupport.uniqueEmail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.atomic.AtomicReference;

import br.com.academia.api.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, PasswordResetIntegrationTest.CapturingMail.class })
class PasswordResetIntegrationTest {

	private static final AtomicReference<String> LINK = new AtomicReference<>();

	@Autowired
	MockMvc mvc;

	@Test
	void resetLinkChangesThePasswordAndUnknownEmailStaysSilent() throws Exception {
		String email = uniqueEmail();
		mvc.perform(json(post(CTX + "/auth/register-academy"), registerBody(email)))
				.andExpect(status().isCreated());

		LINK.set(null);
		mvc.perform(json(post(CTX + "/auth/forgot-password"), "{\"email\":\"ninguem@teste.dev\"}"))
				.andExpect(status().isNoContent());
		if (LINK.get() != null) {
			throw new AssertionError("e-mail desconhecido não pode gerar link");
		}

		mvc.perform(json(post(CTX + "/auth/forgot-password"), "{\"email\":\"" + email + "\"}"))
				.andExpect(status().isNoContent());
		String token = tokenOf(LINK.get());

		mvc.perform(json(post(CTX + "/auth/reset-password"), """
				{"token":"token-invalido","password":"outra-senha-123"}
				"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_RESET_TOKEN"));

		mvc.perform(json(post(CTX + "/auth/reset-password"), """
				{"token":"%s","password":"outra-senha-123"}
				""".formatted(token)))
				.andExpect(status().isNoContent());

		mvc.perform(json(post(CTX + "/auth/login"), """
				{"email":"%s","password":"%s"}
				""".formatted(email, PASSWORD)))
				.andExpect(status().isUnauthorized());
		mvc.perform(json(post(CTX + "/auth/login"), """
				{"email":"%s","password":"outra-senha-123"}
				""".formatted(email)))
				.andExpect(status().isOk());
	}

	private static String tokenOf(String link) {
		int at = link.indexOf("token=");
		if (at < 0) throw new AssertionError("link sem token");
		return link.substring(at + "token=".length());
	}

	@TestConfiguration
	static class CapturingMail {

		@Bean
		@Primary
		PasswordResetMail passwordResetMail() {
			return new PasswordResetMail() {
				@Override
				public boolean enabled() {
					return true;
				}

				@Override
				public void sendResetLink(String email, String link) {
					LINK.set(link);
				}
			};
		}
	}
}
