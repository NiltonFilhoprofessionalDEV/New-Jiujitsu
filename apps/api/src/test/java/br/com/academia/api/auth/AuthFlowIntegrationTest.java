package br.com.academia.api.auth;

import static br.com.academia.api.ApiTestSupport.CTX;
import static br.com.academia.api.ApiTestSupport.PASSWORD;
import static br.com.academia.api.ApiTestSupport.json;
import static br.com.academia.api.ApiTestSupport.read;
import static br.com.academia.api.ApiTestSupport.registerBody;
import static br.com.academia.api.ApiTestSupport.uniqueEmail;
import static br.com.academia.api.ApiTestSupport.withCtx;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AuthFlowIntegrationTest {

	@Autowired
	MockMvc mvc;

	@Test
	void registerCreatesOwnerSessionWithHttpOnlyRefreshCookie() throws Exception {
		String email = uniqueEmail();

		MvcResult result = mvc.perform(json(post(CTX + "/auth/register-academy"), registerBody(email)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.memberships[0].role").value("OWNER"))
				.andExpect(jsonPath("$.memberships[0].academy.name").value("Academia Teste"))
				.andReturn();

		Cookie cookie = result.getResponse().getCookie(AuthController.REFRESH_COOKIE);
		assertThat(cookie).isNotNull();
		assertThat(cookie.isHttpOnly()).isTrue();
		assertThat(cookie.getSecure()).isTrue();
		assertThat(result.getResponse().getContentAsString()).doesNotContain(cookie.getValue());

		String token = read(result, "$.accessToken");
		mvc.perform(withCtx(get(CTX + "/me")).header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.academyName").value("Academia Teste"))
				.andExpect(jsonPath("$.role").value("OWNER"));
	}

	@Test
	void protectedEndpointRejectsMissingToken() throws Exception {
		mvc.perform(withCtx(get(CTX + "/me"))).andExpect(status().isUnauthorized());
	}

	@Test
	void duplicateEmailIsRejected() throws Exception {
		String email = uniqueEmail();
		mvc.perform(json(post(CTX + "/auth/register-academy"), registerBody(email))).andExpect(status().isCreated());

		mvc.perform(json(post(CTX + "/auth/register-academy"), registerBody(email.toUpperCase())))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("EMAIL_IN_USE"));
	}

	@Test
	void loginRejectsWrongPasswordAndAcceptsCorrectOne() throws Exception {
		String email = uniqueEmail();
		mvc.perform(json(post(CTX + "/auth/register-academy"), registerBody(email))).andExpect(status().isCreated());

		mvc.perform(json(post(CTX + "/auth/login"), loginBody(email, "senha-errada-999")))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));

		mvc.perform(json(post(CTX + "/auth/login"), loginBody(email, PASSWORD)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty());
	}

	@Test
	void refreshRotatesTokenAndReuseRevokesAllSessions() throws Exception {
		MvcResult registered = mvc.perform(json(post(CTX + "/auth/register-academy"), registerBody(uniqueEmail())))
				.andExpect(status().isCreated()).andReturn();
		Cookie original = registered.getResponse().getCookie(AuthController.REFRESH_COOKIE);

		MvcResult refreshed = mvc.perform(withCtx(post(CTX + "/auth/refresh")).cookie(original))
				.andExpect(status().isOk()).andReturn();
		Cookie rotated = refreshed.getResponse().getCookie(AuthController.REFRESH_COOKIE);
		assertThat(rotated.getValue()).isNotEqualTo(original.getValue());

		mvc.perform(withCtx(post(CTX + "/auth/refresh")).cookie(original))
				.andExpect(status().isUnauthorized());
		mvc.perform(withCtx(post(CTX + "/auth/refresh")).cookie(rotated))
				.andExpect(status().isUnauthorized());
	}

	private static String loginBody(String email, String password) {
		return """
				{"email":"%s","password":"%s"}
				""".formatted(email, password);
	}
}
