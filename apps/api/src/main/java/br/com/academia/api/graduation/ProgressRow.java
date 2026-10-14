package br.com.academia.api.graduation;

import java.time.LocalDate;
import java.util.UUID;

import br.com.academia.api.student.Belt;

/**
 * Situação de um aluno ativo: desde quando está na faixa e na última graduação (faixa ou grau),
 * com os treinos contados desde cada data (presenças + "treinos anteriores" informados na migração).
 */
record ProgressRow(
		UUID studentId,
		String name,
		String nickname,
		Belt belt,
		int degrees,
		LocalDate beltSince,
		long trainingsSinceBelt,
		LocalDate lastSince,
		long trainingsSinceLast) {
}
