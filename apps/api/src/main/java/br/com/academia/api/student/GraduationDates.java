package br.com.academia.api.student;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.FieldError;

/**
 * Datas que iniciam a contagem quando ainda não há graduação registrada pelo app.
 * Faixa branca sem grau conta da matrícula. Nos demais casos, a faixa conta do dia em que ela foi
 * recebida e o próximo grau conta do dia do último grau.
 */
final class GraduationDates {

	record RankDates(LocalDate beltAwardedAt, LocalDate degreeAwardedAt) {
	}

	private GraduationDates() {
	}

	static boolean needsBeltDate(Belt belt, int degrees) {
		return belt != null && !(belt == Belt.BRANCA && degrees == 0);
	}

	static boolean needsDegreeDate(Belt belt, int degrees) {
		return belt != null && degrees > 0;
	}

	/** {@code required} falso no cadastro público: a data pode ser completada na aprovação ou na ficha. */
	static RankDates resolve(Belt belt, int degrees, LocalDate beltAwardedAt, LocalDate degreeAwardedAt,
			boolean required) {
		if (!needsBeltDate(belt, degrees)) {
			return new RankDates(null, null);
		}
		LocalDate degree = needsDegreeDate(belt, degrees) ? degreeAwardedAt : null;
		if (required) {
			require(beltAwardedAt, degree, needsDegreeDate(belt, degrees));
		}
		return new RankDates(beltAwardedAt, degree);
	}

	private static void require(LocalDate beltAwardedAt, LocalDate degreeAwardedAt, boolean degreeRequired) {
		List<FieldError> errors = new ArrayList<>();
		if (beltAwardedAt == null) {
			errors.add(new FieldError("beltAwardedAt", "BELT_DATE_REQUIRED",
					"Informe o dia em que o aluno recebeu esta faixa"));
		}
		if (degreeRequired && degreeAwardedAt == null) {
			errors.add(new FieldError("degreeAwardedAt", "DEGREE_DATE_REQUIRED",
					"Informe o dia do último grau"));
		}
		if (beltAwardedAt != null && degreeAwardedAt != null && degreeAwardedAt.isBefore(beltAwardedAt)) {
			errors.add(new FieldError("degreeAwardedAt", "DEGREE_BEFORE_BELT",
					"O último grau não pode ser anterior ao dia da faixa"));
		}
		if (!errors.isEmpty()) {
			throw ApiException.invalidFields(errors);
		}
	}
}
