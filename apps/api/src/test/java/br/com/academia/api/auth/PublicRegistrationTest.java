package br.com.academia.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import br.com.academia.api.common.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class PublicRegistrationTest {

	@Test
	void closedRegistrationIsRejected() {
		ApiException error = catchThrowableOfType(ApiException.class, () -> PublicRegistration.requireOpen(false));

		assertThat(error.status()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(error.code()).isEqualTo("REGISTRATION_CLOSED");
	}

	@Test
	void openRegistrationPasses() {
		assertThatCode(() -> PublicRegistration.requireOpen(true)).doesNotThrowAnyException();
	}
}
