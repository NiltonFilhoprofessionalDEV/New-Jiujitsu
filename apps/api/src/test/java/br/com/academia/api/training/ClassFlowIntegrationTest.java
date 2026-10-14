package br.com.academia.api.training;

import static br.com.academia.api.ApiTestSupport.CTX;
import static br.com.academia.api.ApiTestSupport.auth;
import static br.com.academia.api.ApiTestSupport.authJson;
import static br.com.academia.api.ApiTestSupport.read;
import static br.com.academia.api.ApiTestSupport.registerOwner;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

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
class ClassFlowIntegrationTest {

	private static final String EVERY_DAY = "[1,2,3,4,5,6,7]";

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
	void createsUpdatesAndArchivesClass() throws Exception {
		String ownerMembership = read(mvc.perform(auth(get(CTX + "/staff"), owner))
				.andExpect(jsonPath("$", hasSize(1)))
				.andReturn(), "$[0].membershipId");

		mvc.perform(authJson(post(CTX + "/classes"), owner, classBody("Sem dias", "[]", "19:00", null, null, null)))
				.andExpect(status().isBadRequest());
		mvc.perform(authJson(post(CTX + "/classes"), owner, classBody("Invertida", "[1]", "19:00", "18:00", null, null)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_CLASS_TIME"));
		mvc.perform(authJson(post(CTX + "/classes"), owner,
				classBody("Prof alheio", "[1]", "19:00", null, UUID.randomUUID().toString(), null)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_INSTRUCTOR"));

		String id = read(mvc.perform(authJson(post(CTX + "/classes"), owner,
				classBody("  Fundamentos  ", "[5,1,3,1]", "19:00", "20:30", ownerMembership, 20)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("Fundamentos"))
				.andExpect(jsonPath("$.weekdays", contains(1, 3, 5)))
				.andExpect(jsonPath("$.instructor.name").value("Gestor Teste"))
				.andReturn(), "$.id");

		mvc.perform(authJson(put(CTX + "/classes/" + id), owner, classBody("No-Gi", "[2]", "07:00", null, null, null)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("No-Gi"))
				.andExpect(jsonPath("$.instructor").doesNotExist());

		mvc.perform(auth(delete(CTX + "/classes/" + id), owner)).andExpect(status().isNoContent());
		mvc.perform(auth(get(CTX + "/classes"), owner)).andExpect(jsonPath("$", hasSize(0)));
		mvc.perform(auth(get(CTX + "/classes/" + id + "/students"), owner))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("CLASS_NOT_FOUND"));
	}

	@Test
	void enrollmentRespectsCapacityAndStudentKind() throws Exception {
		String classId = createClass(EVERY_DAY, 1);
		String ana = createStudent("TRAINING", "Ana");
		String bruno = createStudent("TRAINING", "Bruno");
		String responsible = createStudent("RESPONSIBLE", "Carla");

		enroll(classId, ana).andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Ana"));
		enroll(classId, ana).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("ALREADY_ENROLLED"));
		enroll(classId, bruno).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("CLASS_FULL"));
		enroll(classId, responsible)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("STUDENT_NOT_TRAINING"));

		mvc.perform(auth(get(CTX + "/classes"), owner)).andExpect(jsonPath("$[0].enrolledCount").value(1));
		mvc.perform(auth(delete(CTX + "/classes/" + classId + "/students/" + ana), owner))
				.andExpect(jsonPath("$", hasSize(0)));
		enroll(classId, bruno).andExpect(status().isOk());
	}

	@Test
	void instructorTakesAttendanceWithWalkInsAndNotes() throws Exception {
		String classId = createClass(EVERY_DAY, null);
		String ana = createStudent("TRAINING", "Ana");
		String bruno = createStudent("TRAINING", "Bruno");
		enroll(classId, ana).andExpect(status().isOk());
		String instructor = staff.instructorTokenFor(mvc, owner);
		String session = CTX + "/attendance/classes/" + classId + "/sessions/" + today;

		mvc.perform(auth(get(session), instructor))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.canMark").value(true))
				.andExpect(jsonPath("$.roster", hasSize(1)))
				.andExpect(jsonPath("$.roster[0].present").value(false));

		mvc.perform(authJson(put(session), instructor, saveBody("Treino de passagem", ana, bruno)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.notes").value("Treino de passagem"))
				.andExpect(jsonPath("$.roster[?(@.present == true)]", hasSize(2)))
				.andExpect(jsonPath("$.roster[1].student.name").value("Bruno"))
				.andExpect(jsonPath("$.roster[1].enrolled").value(false));
		mvc.perform(auth(get(CTX + "/attendance/day?date=" + today), instructor))
				.andExpect(jsonPath("$[0].enrolled").value(1))
				.andExpect(jsonPath("$[0].present").value(2));

		mvc.perform(authJson(put(session), instructor, saveBody(null, ana)))
				.andExpect(jsonPath("$.roster", hasSize(1)))
				.andExpect(jsonPath("$.notes").doesNotExist());
		mvc.perform(authJson(post(CTX + "/classes"), instructor, classBody("X", "[1]", "19:00", null, null, null)))
				.andExpect(status().isForbidden());
	}

	@Test
	void rejectsFutureAttendanceAndDatesWithoutClass() throws Exception {
		String classId = createClass("[" + today.getDayOfWeek().getValue() + "]", null);
		String ana = createStudent("TRAINING", "Ana");
		String sessions = CTX + "/attendance/classes/" + classId + "/sessions/";

		mvc.perform(authJson(put(sessions + today.plusWeeks(1)), owner, saveBody(null, ana)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("ATTENDANCE_IN_FUTURE"));
		mvc.perform(authJson(put(sessions + today.plusWeeks(1)), owner, saveBody("Aula especial")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.canMark").value(false));
		mvc.perform(auth(get(sessions + today.plusDays(1)), owner))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("CLASS_NOT_ON_DATE"));
		mvc.perform(auth(get(CTX + "/attendance/day?date=" + today.plusDays(1)), owner))
				.andExpect(jsonPath("$", hasSize(0)));
	}

	private String createClass(String weekdays, Integer capacity) throws Exception {
		return read(mvc.perform(authJson(post(CTX + "/classes"), owner,
				classBody("Turma", weekdays, "19:00", null, null, capacity)))
				.andExpect(status().isCreated())
				.andReturn(), "$.id");
	}

	private String createStudent(String kind, String name) throws Exception {
		return read(mvc.perform(authJson(post(CTX + "/students"), owner, """
				{"kind":"%s","name":"%s"}
				""".formatted(kind, name)))
				.andExpect(status().isCreated())
				.andReturn(), "$.id");
	}

	private ResultActions enroll(String classId, String studentId) throws Exception {
		return mvc.perform(authJson(post(CTX + "/classes/" + classId + "/students"), owner,
				"{\"studentId\":\"" + studentId + "\"}"));
	}

	private static String classBody(String title, String weekdays, String start, String end, String instructor,
			Integer capacity) {
		return """
				{"title":"%s","weekdays":%s,"startTime":"%s","endTime":%s,"instructorMembershipId":%s,"capacity":%s}
				""".formatted(title, weekdays, start, quoted(end), quoted(instructor), capacity);
	}

	private static String saveBody(String notes, String... studentIds) {
		String ids = String.join("\",\"", studentIds);
		return """
				{"presentStudentIds":[%s],"notes":%s}
				""".formatted(studentIds.length == 0 ? "" : "\"" + ids + "\"", quoted(notes));
	}

	private static String quoted(String value) {
		return value == null ? "null" : "\"" + value + "\"";
	}
}
