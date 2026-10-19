package br.com.academia.api.student;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import br.com.academia.api.common.ApiException;
import org.junit.jupiter.api.Test;

class GraduationDatesTest {

	private static final LocalDate BELT_DAY = LocalDate.of(2026, 8, 17);
	private static final LocalDate DEGREE_DAY = LocalDate.of(2026, 10, 1);

	@Test
	void whiteBeltWithoutDegreeIgnoresDates() {
		GraduationDates.RankDates dates = GraduationDates.resolve(Belt.BRANCA, 0, BELT_DAY, DEGREE_DAY, true);

		assertThat(dates.beltAwardedAt()).isNull();
		assertThat(dates.degreeAwardedAt()).isNull();
	}

	@Test
	void coloredBeltWithoutDegreeKeepsOnlyTheBeltDay() {
		GraduationDates.RankDates dates = GraduationDates.resolve(Belt.AZUL, 0, BELT_DAY, DEGREE_DAY, true);

		assertThat(dates.beltAwardedAt()).isEqualTo(BELT_DAY);
		assertThat(dates.degreeAwardedAt()).isNull();
	}

	@Test
	void degreesNeedBothDatesAndTheDegreeCannotPrecedeTheBelt() {
		assertThat(GraduationDates.resolve(Belt.AZUL, 2, BELT_DAY, DEGREE_DAY, true).degreeAwardedAt())
				.isEqualTo(DEGREE_DAY);

		assertThatThrownBy(() -> GraduationDates.resolve(Belt.AZUL, 2, DEGREE_DAY, BELT_DAY, true))
				.isInstanceOf(ApiException.class)
				.extracting(e -> ((ApiException) e).code())
				.isEqualTo("DEGREE_BEFORE_BELT");
	}

	@Test
	void missingBeltDateIsReportedWhenRequired() {
		assertThatThrownBy(() -> GraduationDates.resolve(Belt.AZUL, 0, null, null, true))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).fields()).containsOnlyKeys("beltAwardedAt"));
	}

	@Test
	void signupMayOmitTheDate() {
		GraduationDates.RankDates dates = GraduationDates.resolve(Belt.AZUL, 1, null, null, false);

		assertThat(dates.beltAwardedAt()).isNull();
		assertThat(dates.degreeAwardedAt()).isNull();
	}
}
