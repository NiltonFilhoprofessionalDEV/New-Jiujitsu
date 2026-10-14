package br.com.academia.api.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CpfTest {

	@Test
	void normalizesMaskedValidCpfToDigits() {
		assertThat(Cpf.normalize("529.982.247-25")).isEqualTo("52998224725");
	}

	@Test
	void blankCpfIsAbsent() {
		assertThat(Cpf.normalize("  ")).isNull();
		assertThat(Cpf.normalize(null)).isNull();
	}

	@ParameterizedTest
	@ValueSource(strings = {"529.982.247-24", "111.111.111-11", "1234567890", "123456789012"})
	void rejectsInvalidCpf(String raw) {
		assertThatThrownBy(() -> Cpf.normalize(raw))
				.isInstanceOf(ApiException.class)
				.extracting(e -> ((ApiException) e).code())
				.isEqualTo("INVALID_CPF");
	}
}
