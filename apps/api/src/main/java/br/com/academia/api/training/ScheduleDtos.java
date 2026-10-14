package br.com.academia.api.training;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import br.com.academia.api.training.ClassDtos.StaffRef;

/** Visões das aulas do ponto de vista de um aluno (área do aluno). */
public final class ScheduleDtos {

	private ScheduleDtos() {
	}

	public record ScheduledClass(
			UUID classId,
			String title,
			List<Integer> weekdays,
			LocalTime startTime,
			LocalTime endTime,
			String location,
			StaffRef instructor) {
	}

	/**
	 * Aula de hoje com o estado do check-in. {@code canUndo}: a presença foi marcada pelo próprio aluno (a do
	 * professor só ele desfaz).
	 */
	public record TodayClass(
			UUID classId,
			String title,
			LocalTime startTime,
			LocalTime endTime,
			String location,
			StaffRef instructor,
			boolean enrolled,
			boolean checkedIn,
			boolean canUndo,
			LocalTime opensAt,
			LocalTime closesAt,
			boolean open) {
	}

	public record AttendanceItem(LocalDate date, String title) {
	}
}
