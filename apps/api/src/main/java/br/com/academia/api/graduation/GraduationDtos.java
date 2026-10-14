package br.com.academia.api.graduation;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import br.com.academia.api.graduation.Graduation.Kind;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class GraduationDtos {

	private GraduationDtos() {
	}

	/**
	 * {@code belt} só vale para {@code BELT}; em {@code DEGREE} o grau é na faixa atual.
	 * {@code exception} libera a ordem de faixas/graus (decisão do gestor), nunca a data.
	 */
	public record GraduationRequest(
			@NotNull Kind kind,
			@Size(max = 30) String belt,
			@NotNull @Min(0) @Max(10) Integer degrees,
			@NotNull LocalDate graduatedOn,
			@Min(0) @Max(10000) Integer priorTrainings,
			Boolean exception,
			@Size(max = 500) String note) {
	}

	/** {@code removable}: só a mais recente pode ser removida, pois desfaz o estado atual do aluno. */
	public record GraduationView(
			UUID id,
			Kind kind,
			String belt,
			int degrees,
			LocalDate graduatedOn,
			Integer priorTrainings,
			boolean exception,
			String note,
			String recordedBy,
			boolean removable) {
	}

	public record RuleTarget(Integer minMonths, Integer minTrainings, Integer trainingsPerDegree) {
	}

	/** {@code ready}: tipo de graduação para a qual o aluno já cumpre as regras, ou {@code null}. */
	public record Progress(
			LocalDate beltSince,
			long monthsAtBelt,
			long trainingsSinceBelt,
			LocalDate lastSince,
			long trainingsSinceLast,
			Kind ready,
			RuleTarget rule) {
	}

	public record StudentGraduations(List<GraduationView> history, Progress progress) {
	}

	/** {@code since}, {@code months} e {@code trainings} referem-se ao critério de {@code kind}. */
	public record ReadyStudent(
			UUID studentId,
			String name,
			String nickname,
			String belt,
			int degrees,
			Kind kind,
			LocalDate since,
			long months,
			long trainings) {
	}
}
