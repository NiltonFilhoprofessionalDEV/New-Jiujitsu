package br.com.academia.api.billing;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class DueDatesTest {

	private static final LocalDate TODAY = LocalDate.of(2026, 1, 20);

	@Test
	void nextOccurrenceIncludesToday() {
		assertThat(DueDates.nextOccurrence(20, TODAY)).isEqualTo(TODAY);
		assertThat(DueDates.nextOccurrence(25, TODAY)).isEqualTo(LocalDate.of(2026, 1, 25));
		assertThat(DueDates.nextOccurrence(5, TODAY)).isEqualTo(LocalDate.of(2026, 2, 5));
	}

	@Test
	void advanceKeepsTheStudentDayAcrossShortMonths() {
		assertThat(DueDates.advance(LocalDate.of(2026, 1, 28), 1, 28)).isEqualTo(LocalDate.of(2026, 2, 28));
		assertThat(DueDates.advance(LocalDate.of(2026, 11, 10), 3, 10)).isEqualTo(LocalDate.of(2027, 2, 10));
	}

	@Test
	void advanceRealignsAnAgreedFirstDueToTheStudentDay() {
		assertThat(DueDates.advance(LocalDate.of(2026, 1, 15), 1, 5)).isEqualTo(LocalDate.of(2026, 2, 5));
	}

	@Test
	void moveToDayStaysInTheMonthUnlessTheDateAlreadyPassed() {
		assertThat(DueDates.moveToDay(LocalDate.of(2026, 2, 10), 25, TODAY)).isEqualTo(LocalDate.of(2026, 2, 25));
		assertThat(DueDates.moveToDay(LocalDate.of(2026, 1, 25), 10, TODAY)).isEqualTo(LocalDate.of(2026, 2, 10));
	}
}
