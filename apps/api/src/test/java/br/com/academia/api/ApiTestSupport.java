package br.com.academia.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Helpers dos testes de integração: context path da API, JSON e sessão de gestor de uma academia nova. */
public final class ApiTestSupport {

	public static final String CTX = "/api/v2";
	public static final String PASSWORD = "senha-forte-123";
	/** Telefone de emergência válido, obrigatório para quem treina. */
	public static final String EMERGENCY_PHONE = "(11) 98888-7777";

	private ApiTestSupport() {
	}

	public static MockHttpServletRequestBuilder withCtx(MockHttpServletRequestBuilder builder) {
		return builder.contextPath(CTX);
	}

	public static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder builder, String body) {
		return withCtx(builder).contentType(MediaType.APPLICATION_JSON).content(body);
	}

	public static MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder builder, String token) {
		return withCtx(builder).header("Authorization", "Bearer " + token);
	}

	public static MockHttpServletRequestBuilder authJson(MockHttpServletRequestBuilder builder, String token,
			String body) {
		return json(builder, body).header("Authorization", "Bearer " + token);
	}

	public static String uniqueEmail() {
		return "gestor-" + UUID.randomUUID() + "@teste.dev";
	}

	public static String registerBody(String email) {
		return """
				{"academyName":"Academia Teste","name":"Gestor Teste","email":"%s","password":"%s"}
				""".formatted(email, PASSWORD);
	}

	/** Cadastra uma academia nova e devolve o access token do dono. */
	public static String registerOwner(MockMvc mvc) throws Exception {
		MvcResult result = mvc.perform(json(post(CTX + "/auth/register-academy"), registerBody(uniqueEmail())))
				.andExpect(status().isCreated())
				.andReturn();
		return read(result, "$.accessToken");
	}

	public static <T> T read(MvcResult result, String path) throws Exception {
		return JsonPath.read(result.getResponse().getContentAsString(), path);
	}
}
