package br.com.academia.api.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PhoneTest {

	@ParameterizedTest
	@ValueSource(strings = {"(11) 3333-4444", "11 98888-7777", "+55 11 98888-7777"})
	void acceptsPhoneWithAreaCode(String raw) {
		assertThat(Phone.isValid(raw)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"98888-7777", "123", "+55 11 98888-77770", ""})
	void rejectsPhoneWithoutAreaCodeOrTooLong(String raw) {
		assertThat(Phone.isValid(raw)).isFalse();
	}
}
