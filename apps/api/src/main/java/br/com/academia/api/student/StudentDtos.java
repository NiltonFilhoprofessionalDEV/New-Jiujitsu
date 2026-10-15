package br.com.academia.api.student;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import br.com.academia.api.student.StudentEnums.BeltRuler;
import br.com.academia.api.student.StudentEnums.Gender;
import br.com.academia.api.student.StudentEnums.Kind;
import br.com.academia.api.student.StudentEnums.Status;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public final class StudentDtos {

	private StudentDtos() {
	}

	/**
	 * Cadastro e edição. Campos de faixa são ignorados para {@link Kind#RESPONSIBLE}. Nome completo e contato
	 * de emergência (de quem treina) são exigidos por {@link StudentRules}.
	 */
	public record StudentRequest(
			@NotNull Kind kind,
			@NotBlank @Size(max = 120) String name,
			@Size(max = 60) String nickname,
			@Email @Size(max = 254) String email,
			@Size(max = 14) String cpf,
			@Size(max = 30) String phone,
			@Size(max = 120) String emergencyContact,
			@Size(max = 30) String emergencyPhone,
			@PastOrPresent LocalDate birthDate,
			Gender gender,
			@Size(max = 30) String belt,
			@Min(0) @Max(10) Integer degrees,
			@PastOrPresent LocalDate degreeAwardedAt,
			BeltRuler beltRuler,
			@PastOrPresent LocalDate enrollmentDate,
			@Size(max = 20) String giSize,
			@Size(max = 20) String beltSize) {
	}

	public record HouseholdRef(UUID id, String name) {

		static HouseholdRef of(Household h) {
			return h == null ? null : new HouseholdRef(h.getId(), h.getName());
		}
	}

	public record StudentListItem(
			UUID id,
			String name,
			String nickname,
			String email,
			String phone,
			String emergencyContact,
			String emergencyPhone,
			Kind kind,
			Status status,
			String belt,
			int degrees,
			LocalDate birthDate,
			LocalDate enrollmentDate,
			HouseholdRef household,
			boolean householdResponsible,
			boolean hasLogin) {

		public static StudentListItem of(Student s) {
			return new StudentListItem(s.getId(), s.getName(), s.getNickname(), s.getEmail(), s.getPhone(),
					s.getEmergencyContact(), s.getEmergencyPhone(), s.getKind(), s.getStatus(), beltSlug(s), s.getDegrees(), s.getBirthDate(),
					s.getEnrollmentDate(), HouseholdRef.of(s.getHousehold()), isResponsible(s), s.getUserId() != null);
		}
	}

	public record StudentDetail(
			UUID id,
			Kind kind,
			Status status,
			String name,
			String nickname,
			String email,
			String cpf,
			String phone,
			String emergencyContact,
			String emergencyPhone,
			LocalDate birthDate,
			Gender gender,
			String belt,
			int degrees,
			LocalDate degreeAwardedAt,
			BeltRuler beltRuler,
			LocalDate enrollmentDate,
			String giSize,
			String beltSize,
			HouseholdRef household,
			boolean householdResponsible,
			boolean hasLogin,
			Instant createdAt) {

		static StudentDetail of(Student s) {
			return new StudentDetail(s.getId(), s.getKind(), s.getStatus(), s.getName(), s.getNickname(),
					s.getEmail(), s.getCpf(), s.getPhone(), s.getEmergencyContact(), s.getEmergencyPhone(),
					s.getBirthDate(), s.getGender(), beltSlug(s), s.getDegrees(), s.getDegreeAwardedAt(), s.getBeltRuler(), s.getEnrollmentDate(),
					s.getGiSize(), s.getBeltSize(), HouseholdRef.of(s.getHousehold()), isResponsible(s),
					s.getUserId() != null, s.getCreatedAt());
		}
	}

	/** Contagens das abas (Todos/Alunos/Responsáveis) e dos inativos escondidos pelo filtro padrão. */
	public record StudentCounts(long all, long training, long responsible, long hiddenInactive) {
	}

	private static String beltSlug(Student s) {
		return s.getBelt() == null ? null : s.getBelt().slug();
	}

	private static boolean isResponsible(Student s) {
		Household h = s.getHousehold();
		return h != null && s.getId().equals(h.getResponsibleStudentId());
	}
}
