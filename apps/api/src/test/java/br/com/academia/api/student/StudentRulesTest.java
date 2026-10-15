package br.com.academia.api.student;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.student.StudentEnums.Kind;
import br.com.academia.api.student.StudentRules.PersonInput;
import org.junit.jupiter.api.Test;

class StudentRulesTest {

	private static final String VALID_PHONE = "(11) 98888-7777";

	@Test
	void formatsNamesAndKeepsPhonesAsTyped() {
		StudentRules.Person person = StudentRules.person(Kind.TRAINING,
				new PersonInput("  maria DA silva ", " ", "joana silva (mãe)", VALID_PHONE));

		assertThat(person.name()).isEqualTo("Maria da Silva");
		assertThat(person.phone()).isNull();
		assertThat(person.emergencyContact()).isEqualTo("Joana Silva (mãe)");
		assertThat(person.emergencyPhone()).isEqualTo(VALID_PHONE);
	}

	@Test
	void responsibleMayLeaveEmergencyContactBlank() {
		StudentRules.Person person = StudentRules.person(Kind.RESPONSIBLE, new PersonInput("Ana Souza", null, " ", null));

		assertThat(person.emergencyContact()).isNull();
		assertThat(person.emergencyPhone()).isNull();
	}

	@Test
	void reportsEveryInvalidFieldAtOnce() {
		assertThatThrownBy(() -> StudentRules.person(Kind.TRAINING, new PersonInput("carlos", "123", null, null)))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> {
					ApiException ex = (ApiException) e;
					assertThat(ex.code()).isEqualTo("FULL_NAME_REQUIRED");
					assertThat(ex.fields()).containsOnlyKeys("name", "phone", "emergencyContact", "emergencyPhone");
				});
	}

	@Test
	void halfFilledEmergencyContactNeedsTheOtherHalf() {
		assertThatThrownBy(() -> StudentRules.person(Kind.RESPONSIBLE, new PersonInput("Ana Souza", null, null, VALID_PHONE)))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> {
					ApiException ex = (ApiException) e;
					assertThat(ex.code()).isEqualTo("EMERGENCY_CONTACT_REQUIRED");
					assertThat(ex.fields()).containsOnlyKeys("emergencyContact");
				});
	}

	@Test
	void invalidEmergencyPhoneIsReportedOnItsField() {
		assertThatThrownBy(() -> StudentRules.person(Kind.RESPONSIBLE, new PersonInput("Ana Souza", null, "Rita", "9999")))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> {
					ApiException ex = (ApiException) e;
					assertThat(ex.code()).isEqualTo("INVALID_PHONE");
					assertThat(ex.fields()).containsOnlyKeys("emergencyPhone");
				});
	}
}
