package br.com.academia.api.report;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import br.com.academia.api.billing.InvoiceDtos.PeriodSummary;

public final class ReportDtos {

	private ReportDtos() {
	}

	public record Period(LocalDate from, LocalDate to) {
	}

	/** Só alunos que treinam; {@code newInPeriod} pela data de matrícula. */
	public record StudentStats(long active, long inactive, long newInPeriod) {
	}

	/** {@code sessions}: aulas dadas no período, ou seja, com pelo menos uma presença. */
	public record AttendanceStats(long checkIns, long sessions, long studentsTrained) {
	}

	/** {@code instructor} é o professor atual da turma; {@code null} quando não há ou saiu da equipe. */
	public record ClassRow(UUID classId, String title, String instructor, long sessions, long checkIns) {
	}

	/** {@code classes}: turmas ativas sob o professor hoje. */
	public record InstructorRow(UUID membershipId, String name, long classes, long sessions, long checkIns) {
	}

	public record StudentRow(UUID studentId, String name, String belt, int degrees, long checkIns) {
	}

	/** {@code lastCheckIn} nulo: nunca treinou desde a matrícula. */
	public record AbsentRow(UUID studentId, String name, LocalDate lastCheckIn) {
	}

	/** Ativos sem treinar desde {@code since}; a lista é limitada, {@code total} não. */
	public record Absent(LocalDate since, long total, List<AbsentRow> students) {
	}

	/** {@code finance} só para gestores; {@code null} para professores. */
	public record Report(
			Period period,
			StudentStats students,
			AttendanceStats attendance,
			List<ClassRow> classes,
			List<InstructorRow> instructors,
			List<StudentRow> topStudents,
			Absent absent,
			PeriodSummary finance) {
	}
}
