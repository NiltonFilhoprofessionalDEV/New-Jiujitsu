package br.com.academia.api.invite;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import br.com.academia.api.common.BcryptSafe;
import br.com.academia.api.invite.EnrollmentLink.State;
import br.com.academia.api.student.StudentEnums.Kind;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public final class InviteDtos {

	private InviteDtos() {
	}

	/** Formulário público: cadastro pelo link de matrícula ou aceite de convite. */
	public record SignupRequest(
			@NotBlank @Size(max = 120) String name,
			@Size(max = 60) String nickname,
			@NotBlank @Email @Size(max = 254) String email,
			@NotBlank @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres") @BcryptSafe String password,
			@Size(max = 14) String cpf,
			@Size(max = 30) String phone,
			@Size(max = 120) String emergencyContact,
			@Size(max = 30) String emergencyPhone,
			@PastOrPresent LocalDate birthDate,
			@Size(max = 30) String belt,
			@Min(0) @Max(10) Integer degrees) {
	}

	public record CreateInviteRequest(
			@NotNull Kind kind,
			@Size(max = 120) String name,
			@Email @Size(max = 254) String email,
			@Size(max = 30) String phone) {
	}

	public record LinkSettings(boolean enabled, LocalDate expiresOn) {
	}

	public record LinkView(String token, boolean enabled, LocalDate expiresOn, int opens, State state) {
	}

	public record InviteView(
			UUID id,
			String token,
			Kind kind,
			String name,
			String email,
			String phone,
			Invite.View status,
			Instant expiresAt,
			int opens,
			Instant createdAt) {

		static InviteView of(Invite i, Instant now) {
			return new InviteView(i.getId(), i.getToken(), i.getKind(), i.getName(), i.getEmail(), i.getPhone(),
					i.view(now), i.getExpiresAt(), i.getOpens(), i.getCreatedAt());
		}
	}

	public record PublicLinkInfo(String academyName, State state) {
	}

	public record PublicInviteInfo(String academyName, Kind kind, String name, String email, Invite.View status) {
	}

	public record EnrollmentRequestView(
			UUID id,
			String name,
			String nickname,
			String email,
			String cpf,
			String phone,
			String emergencyContact,
			String emergencyPhone,
			LocalDate birthDate,
			String belt,
			int degrees,
			Instant createdAt) {

		static EnrollmentRequestView of(EnrollmentRequest r) {
			SignupData d = r.toSignupData();
			return new EnrollmentRequestView(r.getId(), d.name(), d.nickname(), d.email(), d.cpf(), d.phone(),
					d.emergencyContact(), d.emergencyPhone(), d.birthDate(), d.belt(), d.degrees(), r.getCreatedAt());
		}
	}

	public record PendingCount(long pending) {
	}

	/** Faixa confirmada pelo gestor ao aprovar a matrícula. A data inicia a contagem da próxima graduação. */
	public record ApproveEnrollmentRequest(
			@NotBlank @Size(max = 30) String belt,
			@Min(0) @Max(10) int degrees,
			@PastOrPresent LocalDate beltAwardedAt,
			@PastOrPresent LocalDate degreeAwardedAt) {
	}
}
