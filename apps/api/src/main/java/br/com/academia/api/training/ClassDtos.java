package br.com.academia.api.training;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import br.com.academia.api.student.Student;
import br.com.academia.api.student.StudentEnums.Status;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class ClassDtos {

	static final int MAX_CAPACITY = 1000;

	private ClassDtos() {
	}

	/** Dias na ISO-8601 (1 = segunda ... 7 = domingo). Capacidade nula = sem limite. */
	public record ClassRequest(
			@NotBlank @Size(max = 80) String title,
			@Size(max = 500) String description,
			@NotEmpty(message = "Selecione ao menos um dia da semana")
			@Size(max = 7) List<@NotNull @Min(1) @Max(7) Integer> weekdays,
			@NotNull(message = "Informe o horário de início") LocalTime startTime,
			LocalTime endTime,
			@Size(max = 80) String location,
			UUID instructorMembershipId,
			@Min(1) @Max(MAX_CAPACITY) Integer capacity) {
	}

	public record StaffRef(UUID membershipId, String name) {
	}

	public record ClassView(
			UUID id,
			String title,
			String description,
			List<Integer> weekdays,
			LocalTime startTime,
			LocalTime endTime,
			String location,
			StaffRef instructor,
			Integer capacity,
			long enrolledCount) {
	}

	/** Aluno como aparece nas listas de turma e de chamada. */
	public record ClassStudent(UUID id, String name, String nickname, String belt, int degrees, Status status) {

		static ClassStudent of(Student s) {
			return new ClassStudent(s.getId(), s.getName(), s.getNickname(),
					s.getBelt() == null ? null : s.getBelt().slug(), s.getDegrees(), s.getStatus());
		}
	}
}
