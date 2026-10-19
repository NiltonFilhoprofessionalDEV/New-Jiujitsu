package br.com.academia.api.student;

import static br.com.academia.api.ApiTestSupport.CTX;
import static br.com.academia.api.ApiTestSupport.EMERGENCY_CONTACT;
import static br.com.academia.api.ApiTestSupport.EMERGENCY_FIELDS;
import static br.com.academia.api.ApiTestSupport.EMERGENCY_PHONE;
import static br.com.academia.api.ApiTestSupport.auth;
import static br.com.academia.api.ApiTestSupport.authJson;
import static br.com.academia.api.ApiTestSupport.read;
import static br.com.academia.api.ApiTestSupport.registerOwner;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, StaffFixture.class })
class StudentFlowIntegrationTest {

	private static final String VALID_CPF = "529.982.247-25";

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
	void createsStudentAndFindsItBySearchIgnoringAccents() throws Exception {
		String id = createStudent(owner, studentBody(" joão  GONÇALVES ", "Joao@Exemplo.dev", VALID_CPF, "azul", 2));

		mvc.perform(auth(get(CTX + "/students/" + id), owner))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("João Gonçalves"))
				.andExpect(jsonPath("$.emergencyContact").value(EMERGENCY_CONTACT))
				.andExpect(jsonPath("$.emergencyPhone").value(EMERGENCY_PHONE))
				.andExpect(jsonPath("$.email").value("joao@exemplo.dev"))
				.andExpect(jsonPath("$.cpf").value("52998224725"))
				.andExpect(jsonPath("$.belt").value("azul"))
				.andExpect(jsonPath("$.degrees").value(2));

		mvc.perform(auth(get(CTX + "/students/page?q=goncalves"), owner))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.content[0].name").value("João Gonçalves"));
		mvc.perform(auth(get(CTX + "/students/page?q=98224"), owner))
				.andExpect(jsonPath("$.totalElements").value(1));
	}

	@Test
	void studentsAreIsolatedBetweenAcademies() throws Exception {
		String id = createStudent(owner, studentBody("Aluno Privado", null, null, "branca", 0));
		String otherOwner = registerOwner(mvc);

		mvc.perform(auth(get(CTX + "/students/" + id), otherOwner))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("STUDENT_NOT_FOUND"));
		mvc.perform(auth(get(CTX + "/students/page?status=all"), otherOwner))
				.andExpect(jsonPath("$.totalElements").value(0));
		mvc.perform(auth(delete(CTX + "/students/" + id), otherOwner))
				.andExpect(status().isNotFound());
	}

	@Test
	void rejectsDuplicatesAndInvalidData() throws Exception {
		createStudent(owner, studentBody("Primeiro Aluno", "dup@exemplo.dev", VALID_CPF, "branca", 0));

		mvc.perform(authJson(post(CTX + "/students"), owner,
				studentBody("Segundo Aluno", "DUP@exemplo.dev", null, "branca", 0)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("STUDENT_EMAIL_IN_USE"));
		mvc.perform(authJson(post(CTX + "/students"), owner,
				studentBody("Terceiro Aluno", null, VALID_CPF, "branca", 0)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("STUDENT_CPF_IN_USE"));
		mvc.perform(authJson(post(CTX + "/students"), owner,
				studentBody("Quarto Aluno", null, "111.111.111-11", null, 0)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_CPF"));
		mvc.perform(authJson(post(CTX + "/students"), owner, studentBody("Quinto Aluno", null, null, "azul", 5)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_DEGREES"));
		mvc.perform(authJson(post(CTX + "/students"), owner, studentBody("", null, null, "azul", 0)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	@Test
	void requiresFullNameAndEmergencyContactOnlyForWhoTrains() throws Exception {
		mvc.perform(authJson(post(CTX + "/students"), owner, studentBody("Sexto", null, null, "branca", 0)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("FULL_NAME_REQUIRED"))
				.andExpect(jsonPath("$.fields.name").isNotEmpty());
		mvc.perform(authJson(post(CTX + "/students"), owner, """
				{"kind":"TRAINING","name":"Sétimo Aluno","belt":"branca","degrees":0}
				"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("EMERGENCY_CONTACT_REQUIRED"))
				.andExpect(jsonPath("$.fields.emergencyContact").isNotEmpty())
				.andExpect(jsonPath("$.fields.emergencyPhone").isNotEmpty());
		mvc.perform(authJson(post(CTX + "/students"), owner, """
				{"kind":"RESPONSIBLE","name":"Oitavo Aluno","phone":"99","emergencyContact":"Rita Lima"}
				"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_PHONE"))
				.andExpect(jsonPath("$.fields.phone").isNotEmpty())
				.andExpect(jsonPath("$.fields.emergencyPhone").isNotEmpty());
		createStudent(owner, responsibleBody("Responsável sem Emergência"));
	}

	@Test
	void deactivatedStudentLeavesDefaultListAndIsCountedAsHidden() throws Exception {
		String id = createStudent(owner, studentBody("Inativo Teste", null, null, "branca", 0));
		createStudent(owner, responsibleBody("Responsável Teste"));

		mvc.perform(auth(post(CTX + "/students/" + id + "/deactivate"), owner))
				.andExpect(jsonPath("$.status").value("INACTIVE"));

		mvc.perform(auth(get(CTX + "/students/counts"), owner))
				.andExpect(jsonPath("$.all").value(1))
				.andExpect(jsonPath("$.training").value(0))
				.andExpect(jsonPath("$.responsible").value(1))
				.andExpect(jsonPath("$.hiddenInactive").value(1));
		mvc.perform(auth(get(CTX + "/students/page?status=inactive"), owner))
				.andExpect(jsonPath("$.content[0].id").value(id));
		mvc.perform(auth(get(CTX + "/students/page?status=bogus"), owner))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_FILTER"));
	}

	@Test
	void householdLifecycleKeepsStudentsWhenDeleted() throws Exception {
		String parent = createStudent(owner, responsibleBody("Maria Silva"));
		String child = createStudent(owner, studentBody("Ana Silva", null, null, "branca", 0));

		String householdId = read(mvc.perform(authJson(post(CTX + "/households"), owner, """
				{"name":"Silva","responsibleStudentId":"%s","memberIds":["%s"]}
				""".formatted(parent, child)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.members", hasSize(2)))
				.andReturn(), "$.id");

		mvc.perform(authJson(post(CTX + "/households/" + householdId + "/dependents"), owner,
				studentBody("Pedro Silva", null, null, "cinza", 1)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.household.name").value("Silva"));
		mvc.perform(auth(get(CTX + "/students/page?q=silva"), owner))
				.andExpect(jsonPath("$.totalElements").value(3));
		mvc.perform(authJson(post(CTX + "/households"), owner, """
				{"name":"Outra","memberIds":["%s"]}
				""".formatted(child)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("STUDENT_IN_OTHER_HOUSEHOLD"));
		mvc.perform(authJson(post(CTX + "/households"), owner, """
				{"name":"Vazia","memberIds":[]}
				"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("HOUSEHOLD_EMPTY"));

		mvc.perform(auth(delete(CTX + "/households/" + householdId + "/members/" + parent), owner))
				.andExpect(jsonPath("$.responsibleStudentId").value(nullValue()));
		mvc.perform(auth(delete(CTX + "/households/" + householdId), owner)).andExpect(status().isNoContent());
		mvc.perform(auth(get(CTX + "/students/" + child), owner))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.household").value(nullValue()));
	}

	@Test
	void instructorCanReadButNotWrite() throws Exception {
		String instructor = staff.instructorTokenFor(mvc, owner);

		mvc.perform(auth(get(CTX + "/students/page"), instructor)).andExpect(status().isOk());
		mvc.perform(authJson(post(CTX + "/students"), instructor, studentBody("Xis Teste", null, null, "branca", 0)))
				.andExpect(status().isForbidden());
		mvc.perform(authJson(put(CTX + "/households/" + UUID.randomUUID()), instructor, "{\"name\":\"X\"}"))
				.andExpect(status().isForbidden());
	}

	private String createStudent(String token, String body) throws Exception {
		return read(mvc.perform(authJson(post(CTX + "/students"), token, body))
				.andExpect(status().isCreated())
				.andReturn(), "$.id");
	}

	private static String studentBody(String name, String email, String cpf, String belt, int degrees) {
		String dates = belt != null && !(belt.equals("branca") && degrees == 0)
				? ",\"beltAwardedAt\":\"2020-01-01\"" + (degrees > 0 ? ",\"degreeAwardedAt\":\"2020-06-01\"" : "")
				: "";
		return """
				{"kind":"TRAINING","name":"%s","email":%s,"cpf":%s,"belt":%s,"degrees":%d%s,%s}
				""".formatted(name, quoted(email), quoted(cpf), quoted(belt), degrees, dates, EMERGENCY_FIELDS);
	}

	private static String responsibleBody(String name) {
		return """
				{"kind":"RESPONSIBLE","name":"%s"}
				""".formatted(name);
	}

	private static String quoted(String value) {
		return value == null ? "null" : "\"" + value + "\"";
	}
}
