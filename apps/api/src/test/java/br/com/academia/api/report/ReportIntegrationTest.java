package br.com.academia.api.report;

import static br.com.academia.api.ApiTestSupport.CTX;
import static br.com.academia.api.ApiTestSupport.EMERGENCY_PHONE;
import static br.com.academia.api.ApiTestSupport.auth;
import static br.com.academia.api.ApiTestSupport.authJson;
import static br.com.academia.api.ApiTestSupport.read;
import static br.com.academia.api.ApiTestSupport.registerOwner;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import br.com.academia.api.StaffFixture;
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
@Import({ TestcontainersConfiguration.class, StaffFixture.class, ReportIntegrationTest.FixedClock.class })
class ReportIntegrationTest {

	/** Quarta-feira; o período padrão vai de 15/09 a 14/10 e "sumido" é quem não treina desde 30/09. */
	static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 14, 19, 30);

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

	@Autowired
	StaffFixture staff;

	String owner;

	@BeforeEach
	void setUp() throws Exception {
		owner = registerOwner(mvc);
	}

	@Test
	void summarizesAttendanceAndFlagsAbsentStudents() throws Exception {
		String regular = createStudent("Aluno Assiduo", "2026-09-01");
		String missing = createStudent("Aluno Sumido", "2026-09-01");
		createStudent("Aluno Novato", null);
		String classId = read(mvc.perform(authJson(post(CTX + "/classes"), owner,
				"{\"title\":\"Noite\",\"weekdays\":[3],\"startTime\":\"19:00\",\"endTime\":null}"))
				.andExpect(status().isCreated()).andReturn(), "$.id");
		markPresent(classId, "2026-09-16", missing);
		markPresent(classId, "2026-10-07", regular);
		markPresent(classId, "2026-10-14", regular);

		mvc.perform(auth(get(CTX + "/reports"), owner))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.period.from").value("2026-09-15"))
				.andExpect(jsonPath("$.period.to").value("2026-10-14"))
				.andExpect(jsonPath("$.students.active").value(3))
				.andExpect(jsonPath("$.students.newInPeriod").value(1))
				.andExpect(jsonPath("$.attendance.checkIns").value(3))
				.andExpect(jsonPath("$.attendance.sessions").value(3))
				.andExpect(jsonPath("$.attendance.studentsTrained").value(2))
				.andExpect(jsonPath("$.classes[0].title").value("Noite"))
				.andExpect(jsonPath("$.classes[0].checkIns").value(3))
				.andExpect(jsonPath("$.topStudents[0].name").value("Aluno Assiduo"))
				.andExpect(jsonPath("$.topStudents[0].checkIns").value(2))
				.andExpect(jsonPath("$.absent.total").value(1))
				.andExpect(jsonPath("$.absent.students[0].name").value("Aluno Sumido"))
				.andExpect(jsonPath("$.absent.students[0].lastCheckIn").value("2026-09-16"))
				.andExpect(jsonPath("$.instructors", hasSize(1)))
				.andExpect(jsonPath("$.finance.overdue.students").value(0));

		mvc.perform(auth(get(CTX + "/reports?from=2026-10-01&to=2026-10-14"), owner))
				.andExpect(jsonPath("$.attendance.checkIns").value(2))
				.andExpect(jsonPath("$.topStudents", hasSize(1)));
	}

	@Test
	void instructorsSeeReportsWithoutFinanceAndPeriodIsValidated() throws Exception {
		String instructor = staff.instructorTokenFor(mvc, owner);
		mvc.perform(auth(get(CTX + "/reports"), instructor))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.finance").value(nullValue()))
				.andExpect(jsonPath("$.instructors", hasSize(2)));

		mvc.perform(auth(get(CTX + "/reports?from=2026-10-10&to=2026-10-01"), owner))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_PERIOD"));
		mvc.perform(auth(get(CTX + "/reports?from=2025-01-01&to=2026-10-14"), owner))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_PERIOD"));
		mvc.perform(auth(get(CTX + "/reports?from=ontem"), owner)).andExpect(status().isBadRequest());
	}

	private String createStudent(String name, String enrollmentDate) throws Exception {
		String date = enrollmentDate == null ? "" : ",\"enrollmentDate\":\"" + enrollmentDate + "\"";
		return read(mvc.perform(authJson(post(CTX + "/students"), owner,
				"{\"kind\":\"TRAINING\",\"name\":\"" + name + "\",\"emergencyPhone\":\"" + EMERGENCY_PHONE + "\""
						+ date + "}"))
				.andExpect(status().isCreated()).andReturn(), "$.id");
	}

	private void markPresent(String classId, String date, String studentId) throws Exception {
		mvc.perform(authJson(put(CTX + "/attendance/classes/" + classId + "/sessions/" + date), owner,
				"{\"presentStudentIds\":[\"" + studentId + "\"]}")).andExpect(status().isOk());
	}
}
