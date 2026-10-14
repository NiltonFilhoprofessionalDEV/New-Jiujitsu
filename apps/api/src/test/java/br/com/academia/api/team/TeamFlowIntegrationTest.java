package br.com.academia.api.team;

import static br.com.academia.api.ApiTestSupport.CTX;
import static br.com.academia.api.ApiTestSupport.PASSWORD;
import static br.com.academia.api.ApiTestSupport.auth;
import static br.com.academia.api.ApiTestSupport.authJson;
import static br.com.academia.api.ApiTestSupport.json;
import static br.com.academia.api.ApiTestSupport.read;
import static br.com.academia.api.ApiTestSupport.registerOwner;
import static br.com.academia.api.ApiTestSupport.uniqueEmail;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import br.com.academia.api.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class TeamFlowIntegrationTest {

	@Autowired
	MockMvc mvc;

	String owner;

	@BeforeEach
	void setUp() throws Exception {
		owner = registerOwner(mvc);
	}

	@Test
	void inviteAcceptPromoteAndRemove() throws Exception {
		String email = uniqueEmail();
		String token = read(invite(owner, "INSTRUCTOR", email).andExpect(status().isCreated()).andReturn(), "$.token");
		mvc.perform(get(CTX + "/public/staff-invites/" + token).contextPath(CTX))
				.andExpect(jsonPath("$.role").value("INSTRUCTOR"))
				.andExpect(jsonPath("$.status").value("PENDING"));

		String instructor = read(acceptNew(token, uniqueEmail()).andExpect(status().isCreated()).andReturn(),
				"$.accessToken");
		acceptNew(token, uniqueEmail()).andExpect(jsonPath("$.code").value("INVITE_NOT_AVAILABLE"));
		mvc.perform(auth(get(CTX + "/team"), instructor)).andExpect(status().isForbidden());

		List<String> others = read(mvc.perform(auth(get(CTX + "/team"), owner))
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[?(@.email == '" + email + "')].role").value("INSTRUCTOR"))
				.andReturn(), "$[?(@.you == false)].membershipId");
		String memberId = others.getFirst();
		mvc.perform(authJson(put(CTX + "/team/" + memberId + "/role"), owner, "{\"role\":\"CO_MANAGER\"}"))
				.andExpect(jsonPath("$.role").value("CO_MANAGER"));

		String coManager = login(email);
		invite(coManager, "CO_MANAGER", null).andExpect(jsonPath("$.code").value("OWNER_ONLY"));
		invite(coManager, "INSTRUCTOR", null).andExpect(status().isCreated());
		mvc.perform(auth(delete(CTX + "/team/" + memberId), coManager))
				.andExpect(jsonPath("$.code").value("CANNOT_REMOVE_SELF"));
		mvc.perform(authJson(put(CTX + "/team/" + memberId + "/role"), coManager, "{\"role\":\"INSTRUCTOR\"}"))
				.andExpect(jsonPath("$.code").value("OWNER_ONLY"));

		mvc.perform(auth(delete(CTX + "/team/" + memberId), owner)).andExpect(status().isNoContent());
		mvc.perform(auth(get(CTX + "/team"), owner)).andExpect(jsonPath("$", hasSize(1)));
	}

	@Test
	void existingAccountJoinsAnotherAcademyOnce() throws Exception {
		String otherOwner = registerOwner(mvc);
		String token = read(invite(owner, "INSTRUCTOR", null).andReturn(), "$.token");

		mvc.perform(auth(post(CTX + "/staff-invites/" + token + "/accept"), otherOwner))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.memberships", hasSize(2)));
		String second = read(invite(owner, "INSTRUCTOR", null).andReturn(), "$.token");
		mvc.perform(auth(post(CTX + "/staff-invites/" + second + "/accept"), otherOwner))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("ALREADY_STAFF"));
	}

	@Test
	void invitesForAnotherEmailOrRevokedAreRejected() throws Exception {
		String token = read(invite(owner, "INSTRUCTOR", uniqueEmail()).andReturn(), "$.token");
		mvc.perform(auth(post(CTX + "/staff-invites/" + token + "/accept"), registerOwner(mvc)))
				.andExpect(jsonPath("$.code").value("INVITE_EMAIL_MISMATCH"));

		String id = read(mvc.perform(auth(get(CTX + "/team/invites"), owner))
				.andExpect(jsonPath("$", hasSize(1))).andReturn(), "$[0].id");
		mvc.perform(auth(delete(CTX + "/team/invites/" + id), owner)).andExpect(status().isNoContent());
		mvc.perform(get(CTX + "/public/staff-invites/" + token).contextPath(CTX))
				.andExpect(jsonPath("$.status").value("REVOKED"));
		invite(owner, "OWNER", null).andExpect(jsonPath("$.code").value("INVALID_ROLE"));
	}

	private ResultActions invite(String token, String role, String email) throws Exception {
		String emailJson = email == null ? "null" : "\"" + email + "\"";
		return mvc.perform(authJson(post(CTX + "/team/invites"), token, """
				{"role":"%s","name":"Prof","email":%s}
				""".formatted(role, emailJson)));
	}

	private ResultActions acceptNew(String token, String email) throws Exception {
		return mvc.perform(json(post(CTX + "/public/staff-invites/" + token + "/accept"), """
				{"name":"Professor Novo","email":"%s","password":"%s"}
				""".formatted(email, PASSWORD)));
	}

	private String login(String email) throws Exception {
		return read(mvc.perform(json(post(CTX + "/auth/login"), """
				{"email":"%s","password":"%s"}
				""".formatted(email, PASSWORD))).andExpect(status().isOk()).andReturn(), "$.accessToken");
	}
}
