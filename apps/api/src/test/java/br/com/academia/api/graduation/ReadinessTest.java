package br.com.academia.api.graduation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.UUID;

import br.com.academia.api.graduation.Graduation.Kind;
import br.com.academia.api.student.Belt;
import org.junit.jupiter.api.Test;

class ReadinessTest {

	private static final LocalDate TODAY = LocalDate.of(2026, 10, 14);
	private static final RuleValues TWELVE_MONTHS = new RuleValues(12, null, null);

	@Test
	void beltReadyOnlyAfterMinimumMonths() {
		assertThat(Readiness.evaluate(row(Belt.BRANCA, 0, 12, 0, 12, 0), TWELVE_MONTHS, TODAY)).isEqualTo(Kind.BELT);
		assertThat(Readiness.evaluate(row(Belt.BRANCA, 0, 11, 0, 11, 0), TWELVE_MONTHS, TODAY)).isNull();
	}

	@Test
	void beltNeedsAllConfiguredCriteria() {
		RuleValues rule = new RuleValues(12, 100, null);
		assertThat(Readiness.evaluate(row(Belt.AZUL, 0, 30, 99, 30, 99), rule, TODAY)).isNull();
		assertThat(Readiness.evaluate(row(Belt.AZUL, 0, 30, 100, 30, 100), rule, TODAY)).isEqualTo(Kind.BELT);
	}

	@Test
	void degreeReadyByTrainingsWhileDegreesRemain() {
		RuleValues rule = new RuleValues(24, null, 30);
		assertThat(Readiness.evaluate(row(Belt.AZUL, 1, 6, 40, 2, 30), rule, TODAY)).isEqualTo(Kind.DEGREE);
		assertThat(Readiness.evaluate(row(Belt.AZUL, 1, 6, 40, 2, 29), rule, TODAY)).isNull();
		assertThat(Readiness.evaluate(row(Belt.AZUL, 4, 6, 40, 2, 30), rule, TODAY)).isNull();
	}

	@Test
	void beltWithoutCriteriaIsNotTracked() {
		assertThat(Readiness.evaluate(row(Belt.BRANCA, 0, 120, 500, 120, 500), RuleValues.NONE, TODAY)).isNull();
		assertThat(Readiness.evaluate(row(Belt.BRANCA, 0, 120, 500, 120, 500), null, TODAY)).isNull();
	}

	@Test
	void blackBeltFollowsIbjjfDegreeTiers() {
		assertThat(Readiness.evaluate(row(Belt.PRETA, 0, 40, 0, 36, 0), null, TODAY)).isEqualTo(Kind.DEGREE);
		assertThat(Readiness.evaluate(row(Belt.PRETA, 3, 100, 0, 40, 0), null, TODAY)).isNull();
		assertThat(Readiness.evaluate(row(Belt.PRETA, 3, 100, 0, 60, 0), null, TODAY)).isEqualTo(Kind.DEGREE);
		assertThat(Readiness.evaluate(row(Belt.PRETA, 6, 300, 0, 83, 0), null, TODAY)).isNull();
		assertThat(Readiness.evaluate(row(Belt.PRETA, 6, 300, 0, 84, 0), null, TODAY)).isEqualTo(Kind.BELT);
	}

	private static ProgressRow row(Belt belt, int degrees, int monthsAtBelt, long trainingsSinceBelt,
			int monthsSinceLast, long trainingsSinceLast) {
		return new ProgressRow(UUID.randomUUID(), "Aluno", null, belt, degrees, TODAY.minusMonths(monthsAtBelt),
				trainingsSinceBelt, TODAY.minusMonths(monthsSinceLast), trainingsSinceLast);
	}
}
