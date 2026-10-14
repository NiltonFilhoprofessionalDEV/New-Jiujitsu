package br.com.academia.api.portal;

import static br.com.academia.api.ApiTestSupport.CTX;
import static br.com.academia.api.ApiTestSupport.PASSWORD;
import static br.com.academia.api.ApiTestSupport.auth;
import static br.com.academia.api.ApiTestSupport.authJson;
import static br.com.academia.api.ApiTestSupport.json;
import static br.com.academia.api.ApiTestSupport.read;
import static br.com.academia.api.ApiTestSupport.registerOwner;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import br.com.academia.api.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
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
@Import({ TestcontainersConfiguration.class, PortalFlowIntegrationTest.FixedClock.class })
class PortalFlowIntegrationTest {

	/** Quarta-feira (dia 3 na ISO-8601), 19:30. */
	static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 14, 19, 30);
	static final String TODAY = NOW.toLocalDate().toString();

	@TestConfiguration
	static class FixedClock {

		@Bean
		@Primary
		Clock fixedClock() {
			ZoneId zone = ZoneId.of("America/Sao_Paulo");
			return Clock.fixed(NOW.atZone(zone).toInstant(), zone);
		}
	}

	@Autowired
	MockMvc mvc;

	String owner;

	@BeforeEach
	void setUp() throws Exception {
		owner = registerOwner(mvc);
	}

	@Test
	void studentChecksInOnlyInsideTheWindowAndCannotUndoStaffMarks() throws Exception {
		String student = acceptInvite("TRAINING");
		String studentId = read(mvc.perform(auth(get(CTX + "/portal/profiles"), student))
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].self").value(true))
				.andReturn(), "$[0].id");
		String evening = createClass("Noite", "[3]", "19:00", "20:30");
		String late = createClass("Tarde da noite", "[3]", "21:00", null);
		String thursday = createClass("Quinta", "[4]", "19:00", null);
		mvc.perform(authJson(post(CTX + "/classes/" + evening + "/students"), owner,
				"{\"studentId\":\"" + studentId + "\"}")).andExpect(status().isOk());

		String base = CTX + "/portal/students/" + studentId;
		mvc.perform(auth(get(base + "/today"), student))
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].title").value("Noite"))
				.andExpect(jsonPath("$[0].enrolled").value(true))
				.andExpect(jsonPath("$[0].open").value(true))
				.andExpect(jsonPath("$[1].open").value(false))
				.andExpect(jsonPath("$[1].opensAt").value("20:00:00"));

		mvc.perform(auth(post(base + "/check-ins/" + late), student))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("CHECK_IN_CLOSED"));
		mvc.perform(auth(post(base + "/check-ins/" + thursday), student))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("CLASS_NOT_TODAY"));
		mvc.perform(auth(post(base + "/check-ins/" + evening), student))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].checkedIn").value(true))
				.andExpect(jsonPath("$[0].canUndo").value(true));

		mvc.perform(auth(get(base), student))
				.andExpect(jsonPath("$.attendanceTotal").value(1))
				.andExpect(jsonPath("$.recentAttendance[0].title").value("Noite"))
				.andExpect(jsonPath("$.classes", hasSize(1)))
				.andExpect(jsonPath("$.student.belt").value("azul"));

		mvc.perform(auth(delete(base + "/check-ins/" + evening), student))
				.andExpect(jsonPath("$[0].checkedIn").value(false));
		mvc.perform(authJson(put(CTX + "/attendance/classes/" + evening + "/sessions/" + TODAY), owner,
				"{\"presentStudentIds\":[\"" + studentId + "\"]}")).andExpect(status().isOk());
		mvc.perform(auth(delete(base + "/check-ins/" + evening), student))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("CHECK_IN_BY_STAFF"));
	}

	@Test
	void responsibleSeesDependentsButNobodyElse() throws Exception {
		String responsible = acceptInvite("RESPONSIBLE");
		String responsibleId = read(mvc.perform(auth(get(CTX + "/portal/profiles"), responsible)).andReturn(),
				"$[0].id");
		String childId = createStudent("Filho");
		String strangerId = createStudent("Outro aluno");
		mvc.perform(authJson(post(CTX + "/households"), owner, """
				{"name":"Família","responsibleStudentId":"%s","memberIds":["%s","%s"]}
				""".formatted(responsibleId, responsibleId, childId))).andExpect(status().isCreated());

		mvc.perform(auth(get(CTX + "/portal/profiles"), responsible))
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].self").value(true))
				.andExpect(jsonPath("$[1].name").value("Filho"));
		mvc.perform(auth(get(CTX + "/portal/students/" + childId), responsible)).andExpect(status().isOk());
		mvc.perform(auth(get(CTX + "/portal/students/" + childId + "/invoices"), responsible))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
		mvc.perform(auth(get(CTX + "/portal/students/" + strangerId), responsible))
				.andExpect(status().isNotFound());
		mvc.perform(auth(get(CTX + "/portal/profiles"), owner)).andExpect(status().isForbidden());
	}

	private String acceptInvite(String kind) throws Exception {
		String token = read(mvc.perform(authJson(post(CTX + "/invites"), owner, "{\"kind\":\"" + kind + "\"}"))
				.andExpect(status().isCreated()).andReturn(), "$.token");
		String belt = "TRAINING".equals(kind) ? ",\"belt\":\"azul\",\"degrees\":1" : "";
		return read(mvc.perform(json(post(CTX + "/public/invites/" + token + "/accept"), """
				{"name":"Aluno Portal","email":"aluno-%s@teste.dev","password":"%s"%s}
				""".formatted(UUID.randomUUID(), PASSWORD, belt)))
				.andExpect(status().isCreated()).andReturn(), "$.accessToken");
	}

	private String createClass(String title, String weekdays, String start, String end) throws Exception {
		String endJson = end == null ? "null" : "\"" + end + "\"";
		return read(mvc.perform(authJson(post(CTX + "/classes"), owner, """
				{"title":"%s","weekdays":%s,"startTime":"%s","endTime":%s}
				""".formatted(title, weekdays, start, endJson)))
				.andExpect(status().isCreated()).andReturn(), "$.id");
	}

	private String createStudent(String name) throws Exception {
		return read(mvc.perform(authJson(post(CTX + "/students"), owner,
				"{\"kind\":\"TRAINING\",\"name\":\"" + name + "\"}"))
				.andExpect(status().isCreated()).andReturn(), "$.id");
	}
}
