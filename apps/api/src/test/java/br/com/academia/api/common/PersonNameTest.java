package br.com.academia.api.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PersonNameTest {

	@ParameterizedTest
	@CsvSource(delimiter = '|', value = {
			"maria da silva|Maria da Silva",
			"  JOÃO   DOS  SANTOS  |João dos Santos",
			"ana-clara d'ávila|Ana-Clara D'Ávila",
			"pedro e paulo souza|Pedro e Paulo Souza",
			"De Souza Lima|De Souza Lima"})
	void formatsLikeADocument(String raw, String expected) {
		assertThat(PersonName.format(raw)).isEqualTo(expected);
	}

	@Test
	void treatsNonBreakingSpaceAsSpace() {
		assertThat(PersonName.format("maria\u00A0\u00A0silva\u00A0")).isEqualTo("Maria Silva");
	}

	@ParameterizedTest
	@ValueSource(strings = {"ana costa", "Ana da Costa", "de souza lima"})
	void fullNameHasAtLeastTwoNames(String raw) {
		assertThat(PersonName.isFull(raw)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"Ana", "  ana  ", "ana da", "da silva", ""})
	void particlesDoNotCountAsSurname(String raw) {
		assertThat(PersonName.isFull(raw)).isFalse();
	}

	@Test
	void nullIsNotAFullName() {
		assertThat(PersonName.isFull(null)).isFalse();
	}
}
