package br.com.academia.api.auth;

import static br.com.academia.api.ApiTestSupport.CTX;
import static br.com.academia.api.ApiTestSupport.PASSWORD;
import static br.com.academia.api.ApiTestSupport.authJson;
import static br.com.academia.api.ApiTestSupport.json;
import static br.com.academia.api.ApiTestSupport.read;
import static br.com.academia.api.ApiTestSupport.registerBody;
import static br.com.academia.api.ApiTestSupport.uniqueEmail;
import static br.com.academia.api.ApiTestSupport.withCtx;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.academia.api.TestcontainersConfiguration;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class PasswordChangeIntegrationTest {

	private static final String NEW_PASSWORD = "outra-senha-456";

	@Autowired
	MockMvc mvc;

	@Test
	void changeRequiresCurrentPasswordAndEndsOtherSessions() throws Exception {
		String email = uniqueEmail();
		mvc.perform(json(post(CTX + "/auth/register-academy"), registerBody(email))).andExpect(status().isCreated());
		MvcResult here = login(email, PASSWORD);
		Cookie otherDevice = login(email, PASSWORD).getResponse().getCookie(AuthController.REFRESH_COOKIE);
		String token = read(here, "$.accessToken");

		changePassword(token, "senha-errada-1", NEW_PASSWORD)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("WRONG_PASSWORD"));
		changePassword(token, PASSWORD, PASSWORD)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("PASSWORD_UNCHANGED"));

		MvcResult changed = changePassword(token, PASSWORD, NEW_PASSWORD).andExpect(status().isOk()).andReturn();
		Cookie renewed = changed.getResponse().getCookie(AuthController.REFRESH_COOKIE);
		assertThat(renewed).isNotNull();

		mvc.perform(withCtx(post(CTX + "/auth/refresh")).cookie(otherDevice)).andExpect(status().isUnauthorized());
		mvc.perform(withCtx(post(CTX + "/auth/refresh")).cookie(renewed)).andExpect(status().isOk());
		mvc.perform(json(post(CTX + "/auth/login"), loginBody(email, PASSWORD))).andExpect(status().isUnauthorized());
		login(email, NEW_PASSWORD);
	}

	@Test
	void shortNewPasswordIsRejected() throws Exception {
		String email = uniqueEmail();
		String token = read(mvc.perform(json(post(CTX + "/auth/register-academy"), registerBody(email)))
				.andExpect(status().isCreated()).andReturn(), "$.accessToken");

		changePassword(token, PASSWORD, "curta")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	private MvcResult login(String email, String password) throws Exception {
		return mvc.perform(json(post(CTX + "/auth/login"), loginBody(email, password)))
				.andExpect(status().isOk()).andReturn();
	}

	private ResultActions changePassword(String token, String current, String next) throws Exception {
		return mvc.perform(authJson(post(CTX + "/auth/password"), token, """
				{"currentPassword":"%s","newPassword":"%s"}
				""".formatted(current, next)));
	}

	private static String loginBody(String email, String password) {
		return """
				{"email":"%s","password":"%s"}
				""".formatted(email, password);
	}
}
