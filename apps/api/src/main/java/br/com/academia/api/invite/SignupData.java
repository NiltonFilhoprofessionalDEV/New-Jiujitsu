package br.com.academia.api.invite;

import java.time.LocalDate;
import java.util.Locale;

import br.com.academia.api.common.Cpf;
import br.com.academia.api.common.TextNormalizer;
import br.com.academia.api.invite.InviteDtos.SignupRequest;
import br.com.academia.api.student.Belt;
import br.com.academia.api.student.StudentDtos.StudentRequest;
import br.com.academia.api.student.StudentEnums.Kind;

/** Dados do cadastro público já normalizados e validados (sem a senha). */
record SignupData(
		String name,
		String nickname,
		String email,
		String cpf,
		String phone,
		LocalDate birthDate,
		String belt,
		int degrees) {

	private static final String DEFAULT_BELT = "branca";

	/** {@code fixedEmail} preenchido (convite nominal) prevalece sobre o e-mail digitado. */
	static SignupData of(SignupRequest r, Kind kind, String fixedEmail) {
		String email = (fixedEmail != null ? fixedEmail : r.email()).trim().toLowerCase(Locale.ROOT);
		Belt belt = kind == Kind.TRAINING ? Belt.fromSlug(blankToDefault(r.belt())) : null;
		int degrees = 0;
		if (belt != null) {
			degrees = r.degrees() == null ? belt.minDegrees() : r.degrees();
			belt.requireValidDegrees(degrees);
		}
		return new SignupData(
				r.name().trim(),
				TextNormalizer.blankToNull(r.nickname()),
				email,
				Cpf.normalize(r.cpf()),
				TextNormalizer.blankToNull(r.phone()),
				r.birthDate(),
				belt == null ? null : belt.slug(),
				degrees);
	}

	StudentRequest toStudentRequest(Kind kind) {
		return new StudentRequest(kind, name, nickname, email, cpf, phone, birthDate, null, belt, degrees,
				null, null, null, null, null);
	}

	private static String blankToDefault(String belt) {
		return belt == null || belt.isBlank() ? DEFAULT_BELT : belt;
	}
}
