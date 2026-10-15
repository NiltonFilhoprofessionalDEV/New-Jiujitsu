package br.com.academia.api.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class TaxIdTest {

	@Test
	void acceptsMaskedCpf() {
		assertThat(TaxId.normalize("529.982.247-25")).isEqualTo("52998224725");
	}

	@Test
	void acceptsMaskedNumericCnpj() {
		assertThat(TaxId.normalize("11.222.333/0001-81")).isEqualTo("11222333000181");
	}

	@Test
	void acceptsAlphanumericCnpjInAnyCase() {
		assertThat(TaxId.normalize("12.abc.345/01de-35")).isEqualTo("12ABC34501DE35");
	}

	@ParameterizedTest
	@ValueSource(strings = { "529.982.247-24", "11.222.333/0001-82", "11.111.111/1111-11", "12ABC34501DE36",
			"1234567", "ABCDEFGHIJK" })
	void rejectsInvalidDocuments(String raw) {
		assertThatThrownBy(() -> TaxId.normalize(raw))
				.isInstanceOf(ApiException.class)
				.extracting(e -> ((ApiException) e).code())
				.isEqualTo("INVALID_TAX_ID");
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "   " })
	void blankIsAbsent(String raw) {
		assertThat(TaxId.normalize(raw)).isNull();
	}
}
