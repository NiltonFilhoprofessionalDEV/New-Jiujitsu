package br.com.academia.api.invite;

import static br.com.academia.api.ApiTestSupport.CTX;
import static br.com.academia.api.ApiTestSupport.PASSWORD;
import static br.com.academia.api.ApiTestSupport.auth;
import static br.com.academia.api.ApiTestSupport.authJson;
import static br.com.academia.api.ApiTestSupport.json;
import static br.com.academia.api.ApiTestSupport.read;
import static br.com.academia.api.ApiTestSupport.registerOwner;
import static br.com.academia.api.ApiTestSupport.withCtx;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import br.com.academia.api.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class InviteFlowIntegrationTest {

	@Autowired
	MockMvc mvc;

	String owner;

	@BeforeEach
	void setUp() throws Exception {
		owner = registerOwner(mvc);
	}

	@Test
	void linkSignupWaitsForApprovalThenLogsIn() throws Exception {
		String token = linkToken(owner);
		mvc.perform(withCtx(get(CTX + "/public/enroll/" + token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.academyName").value("Academia Teste"))
				.andExpect(jsonPath("$.state").value("OPEN"));

		String email = studentEmail();
		mvc.perform(json(post(CTX + "/public/enroll/" + token), signupBody("Ana Souza", email)))
				.andExpect(status().isAccepted());
		mvc.perform(login(email))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("NO_ACTIVE_MEMBERSHIP"));

		mvc.perform(auth(get(CTX + "/requests/count"), owner)).andExpect(jsonPath("$.pending").value(1));
		String requestId = read(mvc.perform(auth(get(CTX + "/requests/enrollments"), owner))
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].belt").value("azul"))
				.andReturn(), "$[0].id");

		String other = registerOwner(mvc);
		mvc.perform(auth(post(CTX + "/requests/enrollments/" + requestId + "/approve"), other))
				.andExpect(status().isNotFound());

		mvc.perform(auth(post(CTX + "/requests/enrollments/" + requestId + "/approve"), owner))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Ana Souza"))
				.andExpect(jsonPath("$.belt").value("azul"));
		mvc.perform(auth(post(CTX + "/requests/enrollments/" + requestId + "/approve"), owner))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("REQUEST_ALREADY_DECIDED"));

		mvc.perform(login(email))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.memberships[0].role").value("STUDENT"));
	}

	@Test
	void pausedLinkRefusesSignupAndRotationKillsOldToken() throws Exception {
		String token = linkToken(owner);
		mvc.perform(authJson(put(CTX + "/invites/link"), owner, "{\"enabled\":false}"))
				.andExpect(jsonPath("$.state").value("PAUSED"));
		mvc.perform(json(post(CTX + "/public/enroll/" + token), signupBody("Bia", studentEmail())))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("ENROLLMENT_CLOSED"));

		String rotated = read(mvc.perform(auth(post(CTX + "/invites/link/rotate"), owner)).andReturn(), "$.token");
		mvc.perform(withCtx(get(CTX + "/public/enroll/" + token))).andExpect(status().isNotFound());
		mvc.perform(withCtx(get(CTX + "/public/enroll/" + rotated))).andExpect(status().isOk());
	}

	@Test
	void inviteAcceptCreatesActiveStudentOnce() throws Exception {
		String invitedEmail = studentEmail();
		String token = read(mvc.perform(authJson(post(CTX + "/invites"), owner, """
				{"kind":"TRAINING","name":"Caio","email":"%s"}
				""".formatted(invitedEmail.toUpperCase())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("PENDING"))
				.andReturn(), "$.token");

		mvc.perform(withCtx(get(CTX + "/public/invites/" + token)))
				.andExpect(jsonPath("$.email").value(invitedEmail))
				.andExpect(jsonPath("$.status").value("PENDING"));

		mvc.perform(json(post(CTX + "/public/invites/" + token + "/accept"), signupBody("Caio Lima", studentEmail())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.user.email").value(invitedEmail));
		mvc.perform(json(post(CTX + "/public/invites/" + token + "/accept"), signupBody("Outro", studentEmail())))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INVITE_NOT_AVAILABLE"));

		mvc.perform(auth(get(CTX + "/invites?status=accepted"), owner)).andExpect(jsonPath("$", hasSize(1)));
		mvc.perform(auth(get(CTX + "/students/page?q=caio"), owner))
				.andExpect(jsonPath("$.totalElements").value(1));
	}

	@Test
	void revokedInviteIsClosedAndStudentsCannotManageInvites() throws Exception {
		String id = read(mvc.perform(authJson(post(CTX + "/invites"), owner, "{\"kind\":\"RESPONSIBLE\"}"))
				.andReturn(), "$.id");
		String token = read(mvc.perform(auth(post(CTX + "/invites/" + id + "/revoke"), owner))
				.andExpect(jsonPath("$.status").value("REVOKED"))
				.andReturn(), "$.token");
		mvc.perform(json(post(CTX + "/public/invites/" + token + "/accept"), signupBody("Rui", studentEmail())))
				.andExpect(status().isConflict());

		String inviteToken = read(mvc.perform(authJson(post(CTX + "/invites"), owner, "{\"kind\":\"TRAINING\"}"))
				.andReturn(), "$.token");
		String studentAccess = read(mvc.perform(json(post(CTX + "/public/invites/" + inviteToken + "/accept"),
				signupBody("Lia", studentEmail()))).andReturn(), "$.accessToken");
		mvc.perform(auth(get(CTX + "/invites"), studentAccess)).andExpect(status().isForbidden());
		mvc.perform(auth(get(CTX + "/students/page"), studentAccess)).andExpect(status().isForbidden());
	}

	private String linkToken(String access) throws Exception {
		return read(mvc.perform(auth(get(CTX + "/invites/link"), access))
				.andExpect(status().isOk())
				.andReturn(), "$.token");
	}

	private MockHttpServletRequestBuilder login(String email) {
		return json(post(CTX + "/auth/login"), """
				{"email":"%s","password":"%s"}
				""".formatted(email, PASSWORD));
	}

	private static String signupBody(String name, String email) {
		return """
				{"name":"%s","email":"%s","password":"%s","belt":"azul","degrees":1}
				""".formatted(name, email, PASSWORD);
	}

	private static String studentEmail() {
		return "aluno-" + UUID.randomUUID() + "@teste.dev";
	}
}
