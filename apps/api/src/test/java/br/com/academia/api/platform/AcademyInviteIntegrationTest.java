package br.com.academia.api.platform;

import static br.com.academia.api.ApiTestSupport.CTX;
import static br.com.academia.api.ApiTestSupport.auth;
import static br.com.academia.api.ApiTestSupport.json;
import static br.com.academia.api.ApiTestSupport.read;
import static br.com.academia.api.ApiTestSupport.registerBody;
import static br.com.academia.api.ApiTestSupport.registerOwner;
import static br.com.academia.api.ApiTestSupport.uniqueEmail;
import static br.com.academia.api.ApiTestSupport.withCtx;
import static br.com.academia.api.TestcontainersConfiguration.PLATFORM_ADMIN_EMAIL;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.academia.api.StaffFixture;
import br.com.academia.api.TestcontainersConfiguration;
import br.com.academia.api.membership.MembershipRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, StaffFixture.class })
class AcademyInviteIntegrationTest {

	@Autowired
	MockMvc mvc;

	@Autowired
	StaffFixture staff;

	@Test
	void onlyThePlatformAdminCanInviteAnAcademy() throws Exception {
		mvc.perform(auth(post(CTX + "/platform/academy-invites"), registerOwner(mvc)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("PLATFORM_ADMIN_ONLY"));

		mvc.perform(auth(post(CTX + "/platform/academy-invites"), adminToken()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.token").isNotEmpty());
	}

	@Test
	void inviteOpensOneAcademyAndCannotBeReused() throws Exception {
		String token = inviteToken();

		mvc.perform(withCtx(get(CTX + "/public/academy-invites/" + token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PENDING"));

		mvc.perform(json(post(CTX + "/public/academy-invites/" + token + "/accept"), registerBody(uniqueEmail())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.memberships[0].role").value("OWNER"))
				.andExpect(jsonPath("$.memberships[0].academy.name").value("Academia Teste"));

		mvc.perform(json(post(CTX + "/public/academy-invites/" + token + "/accept"), registerBody(uniqueEmail())))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INVITE_NOT_AVAILABLE"));
	}

	@Test
	void revokedInviteCannotOpenAnAcademy() throws Exception {
		MvcResult created = createInvite();
		String token = read(created, "$.token");

		mvc.perform(auth(delete(CTX + "/platform/academy-invites/" + read(created, "$.id")), adminToken()))
				.andExpect(status().isNoContent());

		mvc.perform(json(post(CTX + "/public/academy-invites/" + token + "/accept"), registerBody(uniqueEmail())))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INVITE_NOT_AVAILABLE"));
	}

	@Test
	void unknownInviteIsNotFound() throws Exception {
		mvc.perform(withCtx(get(CTX + "/public/academy-invites/nao-existe")))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("INVITE_NOT_FOUND"));
	}

	private String inviteToken() throws Exception {
		return read(createInvite(), "$.token");
	}

	private MvcResult createInvite() throws Exception {
		return mvc.perform(auth(post(CTX + "/platform/academy-invites"), adminToken()))
				.andExpect(status().isCreated())
				.andReturn();
	}

	private String adminToken() throws Exception {
		return staff.memberTokenFor(mvc, registerOwner(mvc), MembershipRole.INSTRUCTOR, PLATFORM_ADMIN_EMAIL);
	}
}
