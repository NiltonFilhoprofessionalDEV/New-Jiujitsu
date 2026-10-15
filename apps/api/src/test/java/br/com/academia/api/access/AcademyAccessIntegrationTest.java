package br.com.academia.api.access;

import static br.com.academia.api.ApiTestSupport.CTX;
import static br.com.academia.api.ApiTestSupport.auth;
import static br.com.academia.api.ApiTestSupport.read;
import static br.com.academia.api.ApiTestSupport.registerOwner;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class AcademyAccessIntegrationTest {

	private static final String STAFF_ENDPOINT = CTX + "/students/counts";

	@Autowired
	MockMvc mvc;

	@Autowired
	StaffFixture staff;

	@Autowired
	JdbcTemplate jdbc;

	@Test
	void newAcademyStartsAFullTrial() throws Exception {
		String owner = registerOwner(mvc);

		mvc.perform(auth(get(CTX + "/academy-access"), owner))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.enforced").value(true))
				.andExpect(jsonPath("$.status").value("TRIAL"))
				.andExpect(jsonPath("$.daysLeft").value(14))
				.andExpect(jsonPath("$.monthlyPrice").value(100.00))
				.andExpect(jsonPath("$.onlinePayment").value(true))
				.andExpect(jsonPath("$.subscribed").value(false));
	}

	@Test
	void suspendedAcademyBlocksTheStaffButNotTheStudents() throws Exception {
		String owner = registerOwner(mvc);
		String instructor = staff.instructorTokenFor(mvc, owner);
		String student = staff.memberTokenFor(mvc, owner, MembershipRole.STUDENT);
		endTrialDaysAgo(owner, 10);

		mvc.perform(auth(get(STAFF_ENDPOINT), owner))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("ACADEMY_SUSPENDED"));
		mvc.perform(auth(get(STAFF_ENDPOINT), instructor))
				.andExpect(jsonPath("$.code").value("ACADEMY_SUSPENDED"));
		mvc.perform(auth(get(CTX + "/me"), owner)).andExpect(status().isOk());
		mvc.perform(auth(get(CTX + "/academy-access"), owner))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUSPENDED"));
		mvc.perform(auth(get(CTX + "/videos"), student)).andExpect(status().isOk());
	}

	@Test
	void graceKeepsTheStaffWorking() throws Exception {
		String owner = registerOwner(mvc);
		endTrialDaysAgo(owner, 2);

		mvc.perform(auth(get(CTX + "/academy-access"), owner))
				.andExpect(jsonPath("$.status").value("GRACE"))
				.andExpect(jsonPath("$.daysLeft").value(3));
		mvc.perform(auth(get(STAFF_ENDPOINT), owner)).andExpect(status().isOk());
	}

	@Test
	void studentsCannotReadTheAcademySubscription() throws Exception {
		String student = staff.memberTokenFor(mvc, registerOwner(mvc), MembershipRole.STUDENT);

		mvc.perform(auth(get(CTX + "/academy-access"), student)).andExpect(status().isForbidden());
	}

	private void endTrialDaysAgo(String ownerToken, int days) throws Exception {
		String academyId = read(mvc.perform(auth(get(CTX + "/me"), ownerToken)).andReturn(), "$.academyId");
		jdbc.update("update academies set trial_ends_at = now() - make_interval(days => ?) where id = ?",
				days, UUID.fromString(academyId));
	}
}
