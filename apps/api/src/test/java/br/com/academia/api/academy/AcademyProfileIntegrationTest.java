package br.com.academia.api.academy;

import static br.com.academia.api.ApiTestSupport.CTX;
import static br.com.academia.api.ApiTestSupport.auth;
import static br.com.academia.api.ApiTestSupport.authJson;
import static br.com.academia.api.ApiTestSupport.registerOwner;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.academia.api.StaffFixture;
import br.com.academia.api.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, StaffFixture.class })
class AcademyProfileIntegrationTest {

	private static final String PROFILE = """
			{"name":"  Academia Nova  ","cnpj":"","email":"contato@academia.dev","phone":"11 99999-0000",
			 "address":"Rua A, 10","city":"   ","state":"SP"}
			""";

	@Autowired
	MockMvc mvc;

	@Autowired
	StaffFixture staff;

	@Test
	void managerUpdatesProfileAndBlankFieldsBecomeNull() throws Exception {
		String owner = registerOwner(mvc);

		mvc.perform(authJson(put(CTX + "/academies/current/profile"), owner, PROFILE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Academia Nova"))
				.andExpect(jsonPath("$.cnpj").doesNotExist())
				.andExpect(jsonPath("$.city").doesNotExist());

		mvc.perform(auth(get(CTX + "/academies/current/profile"), owner))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.phone").value("11 99999-0000"))
				.andExpect(jsonPath("$.state").value("SP"));
	}

	@Test
	void instructorCannotUpdateProfile() throws Exception {
		String instructor = staff.instructorTokenFor(mvc, registerOwner(mvc));

		mvc.perform(authJson(put(CTX + "/academies/current/profile"), instructor, PROFILE))
				.andExpect(status().isForbidden());
	}
}
