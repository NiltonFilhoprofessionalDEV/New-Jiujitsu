package br.com.academia.api.auth;

import java.util.List;
import java.util.UUID;

import br.com.academia.api.membership.Membership;
import br.com.academia.api.membership.MembershipRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

	private AuthDtos() {
	}

	public record RegisterAcademyRequest(
			@NotBlank @Size(max = 120) String academyName,
			@NotBlank @Size(max = 120) String name,
			@NotBlank @Email @Size(max = 254) String email,
			@NotBlank @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres") String password) {
	}

	public record LoginRequest(
			@NotBlank @Email @Size(max = 254) String email,
			@NotBlank @Size(max = 72) String password) {
	}

	public record SwitchAcademyRequest(@NotNull UUID membershipId) {
	}

	public record AcademySummary(UUID id, String name, String slug) {
	}

	public record MembershipSummary(UUID id, MembershipRole role, AcademySummary academy) {

		static MembershipSummary of(Membership m) {
			var a = m.getAcademy();
			return new MembershipSummary(m.getId(), m.getRole(), new AcademySummary(a.getId(), a.getName(), a.getSlug()));
		}
	}

	public record UserSummary(UUID id, String name, String email) {
	}

	public record SessionResponse(
			String accessToken,
			long expiresIn,
			UserSummary user,
			UUID activeMembershipId,
			List<MembershipSummary> memberships) {
	}

	/** Resultado interno: o refresh token vai para o cookie, nunca para o corpo da resposta. */
	public record IssuedSession(SessionResponse body, String refreshToken) {
	}
}
