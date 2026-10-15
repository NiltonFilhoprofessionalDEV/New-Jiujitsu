package br.com.academia.api.platform;

import static br.com.academia.api.ApiTestSupport.CTX;
import static br.com.academia.api.ApiTestSupport.auth;
import static br.com.academia.api.ApiTestSupport.authJson;
import static br.com.academia.api.ApiTestSupport.read;
import static br.com.academia.api.ApiTestSupport.registerOwner;
import static br.com.academia.api.TestcontainersConfiguration.PLATFORM_ADMIN_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import br.com.academia.api.StaffFixture;
import br.com.academia.api.TestcontainersConfiguration;
import br.com.academia.api.membership.MembershipRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, StaffFixture.class })
class PlatformIntegrationTest {

	@Autowired
	MockMvc mvc;

	@Autowired
	StaffFixture staff;

	@Autowired
	JdbcTemplate jdbc;

	@Test
	void onlyTheConfiguredEmailIsAdmin() throws Exception {
		String owner = registerOwner(mvc);

		mvc.perform(auth(get(CTX + "/platform/access"), owner))
				.andExpect(jsonPath("$.admin").value(false));
		mvc.perform(auth(get(CTX + "/platform/access"), adminToken()))
				.andExpect(jsonPath("$.admin").value(true));
		mvc.perform(auth(get(CTX + "/platform/academies"), owner))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("PLATFORM_ADMIN_ONLY"));
	}

	@Test
	void adminListsAcademiesWithOwnerAndStatus() throws Exception {
		String owner = registerOwner(mvc);
		String academyId = academyIdOf(owner);

		mvc.perform(auth(get(CTX + "/platform/academies"), adminToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.id == '%s')].status".formatted(academyId)).value("TRIAL"))
				.andExpect(jsonPath("$[?(@.id == '%s')].ownerName".formatted(academyId)).value("Gestor Teste"));
	}

	@Test
	void trialExtensionReopensASuspendedAcademy() throws Exception {
		String owner = registerOwner(mvc);
		String academyId = academyIdOf(owner);
		jdbc.update("update academies set trial_ends_at = now() - interval '30 days' where id = ?",
				UUID.fromString(academyId));

		mvc.perform(authJson(post(grantsOf(academyId)), adminToken(), """
				{"kind":"TRIAL_EXTENSION","days":30,"note":"Parceria"}
				"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("TRIAL"))
				.andExpect(jsonPath("$.daysLeft").value(30));
		mvc.perform(auth(get(CTX + "/students/counts"), owner)).andExpect(status().isOk());
		assertThat(jdbc.queryForObject("select count(*) from academy_access_grants where academy_id = ?",
				Long.class, UUID.fromString(academyId))).isEqualTo(1);
	}

	@Test
	void manualPaymentActivatesTheAcademy() throws Exception {
		String academyId = academyIdOf(registerOwner(mvc));

		mvc.perform(authJson(post(grantsOf(academyId)), adminToken(), """
				{"kind":"MANUAL_PAYMENT","days":31}
				"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.daysLeft").value(31));
	}

	@Test
	void grantRejectsOutOfRangeDaysAndNonAdmins() throws Exception {
		String owner = registerOwner(mvc);
		String academyId = academyIdOf(owner);

		mvc.perform(authJson(post(grantsOf(academyId)), adminToken(), """
				{"kind":"MANUAL_PAYMENT","days":0}
				"""))
				.andExpect(status().isBadRequest());
		mvc.perform(authJson(post(grantsOf(academyId)), owner, """
				{"kind":"MANUAL_PAYMENT","days":30}
				"""))
				.andExpect(status().isForbidden());
	}

	private String adminToken() throws Exception {
		return staff.memberTokenFor(mvc, registerOwner(mvc), MembershipRole.INSTRUCTOR, PLATFORM_ADMIN_EMAIL);
	}

	private String academyIdOf(String token) throws Exception {
		return read(mvc.perform(auth(get(CTX + "/me"), token)).andReturn(), "$.academyId");
	}

	private static String grantsOf(String academyId) {
		return CTX + "/platform/academies/" + academyId + "/grants";
	}
}
