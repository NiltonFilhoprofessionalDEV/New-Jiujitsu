package br.com.academia.api.billing;

import java.time.LocalDate;

/** O dia de vencimento vai até 28, então existe em todos os meses e {@code withDayOfMonth} nunca falha. */
final class DueDates {

	private DueDates() {
	}

	/** Primeiro dia {@code dueDay} a partir de {@code from}, inclusive. */
	static LocalDate nextOccurrence(int dueDay, LocalDate from) {
		LocalDate candidate = from.withDayOfMonth(dueDay);
		return candidate.isBefore(from) ? candidate.plusMonths(1) : candidate;
	}

	/** Vencimento do ciclo seguinte; um 1º vencimento combinado em outro dia volta ao dia do aluno. */
	static LocalDate advance(LocalDate due, int intervalMonths, int dueDay) {
		return due.plusMonths(intervalMonths).withDayOfMonth(dueDay);
	}

	/** Troca o dia mantendo o mês do próximo vencimento; se a nova data já passou, vai para a próxima. */
	static LocalDate moveToDay(LocalDate nextDue, int dueDay, LocalDate today) {
		LocalDate candidate = nextDue.withDayOfMonth(dueDay);
		return candidate.isBefore(today) ? nextOccurrence(dueDay, today) : candidate;
	}
}
