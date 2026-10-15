package br.com.academia.api.student;

import java.time.LocalDate;

import br.com.academia.api.student.StudentEnums.BeltRuler;
import br.com.academia.api.student.StudentEnums.Gender;
import br.com.academia.api.student.StudentEnums.Kind;

/** Dados do aluno depois de normalizados (e-mail em minúsculas, CPF só dígitos, faixa resolvida). */
record StudentData(
		Kind kind,
		String name,
		String nickname,
		String email,
		String cpf,
		String phone,
		String emergencyContact,
		String emergencyPhone,
		LocalDate birthDate,
		Gender gender,
		Belt belt,
		int degrees,
		LocalDate degreeAwardedAt,
		BeltRuler beltRuler,
		LocalDate enrollmentDate,
		String giSize,
		String beltSize) {
}
