package br.com.academia.api.graduation;

import static br.com.academia.api.ApiTestSupport.CTX;
import static br.com.academia.api.ApiTestSupport.EMERGENCY_FIELDS;
import static br.com.academia.api.ApiTestSupport.auth;
import static br.com.academia.api.ApiTestSupport.authJson;
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
import java.time.LocalDate;

import br.com.academia.api.StaffFixture;
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
@Import({ TestcontainersConfiguration.class, StaffFixture.class })
class GraduationFlowIntegrationTest {

	@Autowired
	MockMvc mvc;

	@Autowired
	StaffFixture staff;

	@Autowired
	Clock clock;

	String owner;
	LocalDate today;

	@BeforeEach
	void setUp() throws Exception {
		owner = registerOwner(mvc);
		today = LocalDate.now(clock);
	}

	@Test
	void registersAndUndoesGraduationsInOrder() throws Exception {
		String id = createStudent("TRAINING", "Ana Lima", today.minusYears(2));
		mvc.perform(auth(get(CTX + "/graduations/ready"), owner))
				.andExpect(jsonPath("$[0].studentId").value(id))
				.andExpect(jsonPath("$[0].kind").value("BELT"));

		graduate(id, "BELT", "azul", 0, today.minusDays(10))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.history", hasSize(1)))
				.andExpect(jsonPath("$.progress.ready").doesNotExist());
		mvc.perform(auth(get(CTX + "/graduations/ready"), owner)).andExpect(jsonPath("$", hasSize(0)));
		String degreeId = read(graduate(id, "DEGREE", null, 1, today).andExpect(status().isCreated()).andReturn(),
				"$.history[0].id");
		expectStudent(id, "azul", 1);

		graduate(id, "DEGREE", null, 1, today).andExpect(jsonPath("$.code").value("INVALID_GRADUATION_SEQUENCE"));
		graduate(id, "BELT", "branca", 0, today).andExpect(jsonPath("$.code").value("INVALID_GRADUATION_SEQUENCE"));
		graduate(id, "DEGREE", null, 2, today.minusDays(20)).andExpect(jsonPath("$.code").value("GRADUATION_BEFORE_LAST"));
		graduate(id, "DEGREE", null, 2, today.plusDays(1)).andExpect(jsonPath("$.code").value("GRADUATION_IN_FUTURE"));

		String beltId = read(mvc.perform(auth(get(CTX + "/students/" + id + "/graduations"), owner)).andReturn(),
				"$.history[1].id");
		mvc.perform(auth(delete(CTX + "/students/" + id + "/graduations/" + beltId), owner))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("GRADUATION_NOT_LATEST"));
		mvc.perform(auth(delete(CTX + "/students/" + id + "/graduations/" + degreeId), owner)).andExpect(status().isOk());
		mvc.perform(auth(delete(CTX + "/students/" + id + "/graduations/" + beltId), owner))
				.andExpect(jsonPath("$.history", hasSize(0)));
		expectStudent(id, "branca", 0);
	}

	@Test
	void customRulesUseAttendanceSinceLastGraduation() throws Exception {
		mvc.perform(authJson(put(CTX + "/graduations/rules"), owner, """
				{"rules":[{"belt":"branca","minMonths":null,"minTrainings":2,"trainingsPerDegree":1}]}
				"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].custom").value(true))
				.andExpect(jsonPath("$[0].minMonths").doesNotExist());
		mvc.perform(authJson(put(CTX + "/graduations/rules"), owner, """
				{"rules":[{"belt":"preta","minMonths":12}]}
				"""))
				.andExpect(jsonPath("$.code").value("INVALID_BELT_RULE"));

		String id = createStudent("TRAINING", "Bia Costa", today);
		markPresentToday(id);

		mvc.perform(auth(get(CTX + "/students/" + id + "/graduations"), owner))
				.andExpect(jsonPath("$.progress.trainingsSinceBelt").value(1))
				.andExpect(jsonPath("$.progress.ready").value("DEGREE"));
		graduate(id, "DEGREE", null, 1, today)
				.andExpect(jsonPath("$.progress.trainingsSinceLast").value(0))
				.andExpect(jsonPath("$.progress.trainingsSinceBelt").value(1))
				.andExpect(jsonPath("$.progress.ready").doesNotExist());
	}

	@Test
	void onlyManagersRegisterAndResponsiblesNeverGraduate() throws Exception {
		String responsible = createStudent("RESPONSIBLE", "Carla Dias", today);
		graduate(responsible, "BELT", "azul", 0, today).andExpect(jsonPath("$.code").value("STUDENT_NOT_TRAINING"));

		String id = createStudent("TRAINING", "Dani Melo", today);
		String instructor = staff.instructorTokenFor(mvc, owner);
		mvc.perform(auth(get(CTX + "/graduations/ready"), instructor)).andExpect(status().isOk());
		mvc.perform(auth(get(CTX + "/students/" + id + "/graduations"), instructor)).andExpect(status().isOk());
		mvc.perform(authJson(post(CTX + "/students/" + id + "/graduations"), instructor,
				graduationBody("BELT", "azul", 0, today))).andExpect(status().isForbidden());
		mvc.perform(authJson(put(CTX + "/graduations/rules"), instructor, "{\"rules\":[]}"))
				.andExpect(status().isForbidden());
	}

	private ResultActions graduate(String studentId, String kind, String belt, int degrees, LocalDate date)
			throws Exception {
		return mvc.perform(authJson(post(CTX + "/students/" + studentId + "/graduations"), owner,
				graduationBody(kind, belt, degrees, date)));
	}

	private void expectStudent(String id, String belt, int degrees) throws Exception {
		mvc.perform(auth(get(CTX + "/students/" + id), owner))
				.andExpect(jsonPath("$.belt").value(belt))
				.andExpect(jsonPath("$.degrees").value(degrees));
	}

	private String createStudent(String kind, String name, LocalDate enrollmentDate) throws Exception {
		return read(mvc.perform(authJson(post(CTX + "/students"), owner, """
				{"kind":"%s","name":"%s","enrollmentDate":"%s",%s}
				""".formatted(kind, name, enrollmentDate, EMERGENCY_FIELDS)))
				.andExpect(status().isCreated())
				.andReturn(), "$.id");
	}

	private void markPresentToday(String studentId) throws Exception {
		String classId = read(mvc.perform(authJson(post(CTX + "/classes"), owner, """
				{"title":"Turma","weekdays":[1,2,3,4,5,6,7],"startTime":"19:00"}
				""")).andExpect(status().isCreated()).andReturn(), "$.id");
		mvc.perform(authJson(put(CTX + "/attendance/classes/" + classId + "/sessions/" + today), owner,
				"{\"presentStudentIds\":[\"" + studentId + "\"]}")).andExpect(status().isOk());
	}

	private static String graduationBody(String kind, String belt, int degrees, LocalDate date) {
		String beltJson = belt == null ? "null" : "\"" + belt + "\"";
		return """
				{"kind":"%s","belt":%s,"degrees":%d,"graduatedOn":"%s"}
				""".formatted(kind, beltJson, degrees, date);
	}
}
