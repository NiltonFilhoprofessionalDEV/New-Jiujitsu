package br.com.academia.api.graduation;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import br.com.academia.api.graduation.Graduation.Kind;
import br.com.academia.api.student.Belt;

/** Decide se o aluno aparece como pronto. Não gradua ninguém: é só o aviso para o gestor. */
final class Readiness {

	/** IBJJF, faixa preta: 3 anos por grau do 1º ao 3º, 5 anos do 4º ao 6º, 7 anos do 6º para a coral. */
	private static final int BLACK_EARLY_MONTHS = 36;
	private static final int BLACK_MID_MONTHS = 60;
	private static final int BLACK_TO_CORAL_MONTHS = 84;
	private static final int BLACK_EARLY_LAST_DEGREE = 2;
	private static final int BLACK_LAST_DEGREE = 6;

	private Readiness() {
	}

	static long monthsBetween(LocalDate since, LocalDate today) {
		return Math.max(0, ChronoUnit.MONTHS.between(since, today));
	}

	/** @return o tipo de graduação para a qual o aluno está pronto, ou {@code null}. */
	static Kind evaluate(ProgressRow row, RuleValues rule, LocalDate today) {
		if (row.belt() == Belt.PRETA) {
			return blackBelt(row, today);
		}
		if (rule == null) {
			return null;
		}
		if (rule.tracksBelt() && meetsBelt(row, rule, today)) {
			return Kind.BELT;
		}
		Integer perDegree = rule.trainingsPerDegree();
		boolean degreeLeft = row.degrees() < row.belt().maxDegrees();
		return perDegree != null && degreeLeft && row.trainingsSinceLast() >= perDegree ? Kind.DEGREE : null;
	}

	private static boolean meetsBelt(ProgressRow row, RuleValues rule, LocalDate today) {
		boolean months = rule.minMonths() == null || monthsBetween(row.beltSince(), today) >= rule.minMonths();
		boolean trainings = rule.minTrainings() == null || row.trainingsSinceBelt() >= rule.minTrainings();
		return months && trainings;
	}

	private static Kind blackBelt(ProgressRow row, LocalDate today) {
		long months = monthsBetween(row.lastSince(), today);
		if (row.degrees() >= BLACK_LAST_DEGREE) {
			return months >= BLACK_TO_CORAL_MONTHS ? Kind.BELT : null;
		}
		int needed = row.degrees() <= BLACK_EARLY_LAST_DEGREE ? BLACK_EARLY_MONTHS : BLACK_MID_MONTHS;
		return months >= needed ? Kind.DEGREE : null;
	}
}
