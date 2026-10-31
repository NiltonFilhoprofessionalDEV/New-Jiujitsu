package br.com.academia.api.training;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import br.com.academia.api.training.ClassDtos.ClassStudent;
import br.com.academia.api.training.ClassDtos.StaffRef;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AttendanceDtos {

	/** Teto de presenças por chamada, para conter payloads abusivos. */
	static final int MAX_PRESENT = 500;

	private AttendanceDtos() {
	}

	/** Aula que acontece na data consultada, com os números para a lista "Aulas do dia". */
	public record DayClass(
			UUID classId,
			String title,
			LocalTime startTime,
			LocalTime endTime,
			String location,
			StaffRef instructor,
			long enrolled,
			long present,
			boolean extra) {
	}

	/** {@code enrolled = false} indica aluno avulso: marcado nesta data sem estar inscrito na turma. */
	public record RosterEntry(ClassStudent student, boolean enrolled, boolean present) {
	}

	public record SessionView(
			UUID classId,
			String title,
			LocalDate date,
			LocalTime startTime,
			LocalTime endTime,
			StaffRef instructor,
			String notes,
			boolean canMark,
			boolean extra,
			List<RosterEntry> roster) {
	}

	/** Substitui a lista de presentes da data pela enviada. */
	public record SaveSessionRequest(
			@NotNull @Size(max = MAX_PRESENT) List<@NotNull UUID> presentStudentIds,
			@Size(max = 2000) String notes) {
	}
}
