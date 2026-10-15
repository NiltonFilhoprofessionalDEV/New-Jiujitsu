package br.com.academia.api.access;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class SuspendedAcademyGuardTest {

	@Test
	void freeAppNeverBlocksNorEvaluatesTheAcademy() {
		AcademyAccessService access = mock(AcademyAccessService.class);
		when(access.enforced()).thenReturn(false);
		SuspendedAcademyGuard guard = new SuspendedAcademyGuard(access);

		boolean proceeds = guard.preHandle(new MockHttpServletRequest("GET", "/students"),
				new MockHttpServletResponse(), new Object());

		assertThat(proceeds).isTrue();
		verify(access, never()).accessOf(any());
	}
}
