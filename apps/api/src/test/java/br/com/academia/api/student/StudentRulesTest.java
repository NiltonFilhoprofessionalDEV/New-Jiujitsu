package br.com.academia.api.student;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.student.StudentEnums.Kind;
import org.junit.jupiter.api.Test;

class StudentRulesTest {

	@Test
	void formatsNameAndKeepsPhonesAsTyped() {
		StudentRules.Person person = StudentRules.person(Kind.TRAINING, "  maria DA silva ", " ", "(11) 98888-7777");

		assertThat(person.name()).isEqualTo("Maria da Silva");
		assertThat(person.phone()).isNull();
		assertThat(person.emergencyPhone()).isEqualTo("(11) 98888-7777");
	}

	@Test
	void responsibleMayLeaveEmergencyPhoneBlank() {
		assertThat(StudentRules.person(Kind.RESPONSIBLE, "Ana Souza", null, null).emergencyPhone()).isNull();
	}

	@Test
	void reportsEveryInvalidFieldAtOnce() {
		assertThatThrownBy(() -> StudentRules.person(Kind.TRAINING, "carlos", "123", null))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> {
					ApiException ex = (ApiException) e;
					assertThat(ex.code()).isEqualTo("FULL_NAME_REQUIRED");
					assertThat(ex.fields()).containsOnlyKeys("name", "phone", "emergencyPhone");
				});
	}

	@Test
	void invalidEmergencyPhoneIsReportedOnItsField() {
		assertThatThrownBy(() -> StudentRules.person(Kind.RESPONSIBLE, "Ana Souza", null, "9999"))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> {
					ApiException ex = (ApiException) e;
					assertThat(ex.code()).isEqualTo("INVALID_PHONE");
					assertThat(ex.fields()).containsOnlyKeys("emergencyPhone");
				});
	}
}
