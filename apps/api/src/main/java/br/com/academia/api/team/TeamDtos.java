package br.com.academia.api.team;

import java.time.Instant;
import java.util.UUID;

import br.com.academia.api.membership.Membership;
import br.com.academia.api.membership.MembershipRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class TeamDtos {

	private TeamDtos() {
	}

	public record TeamMember(
			UUID membershipId,
			String name,
			String email,
			MembershipRole role,
			Instant joinedAt,
			Instant lastUsedAt,
			boolean you) {

		static TeamMember of(Membership m, UUID currentMembershipId) {
			return new TeamMember(m.getId(), m.getUser().getName(), m.getUser().getEmail(), m.getRole(),
					m.getCreatedAt(), m.getLastUsedAt(), m.getId().equals(currentMembershipId));
		}
	}

	/** {@code role}: só {@code INSTRUCTOR} ou {@code CO_MANAGER}; o service valida. */
	public record CreateStaffInviteRequest(
			@NotNull MembershipRole role,
			@Size(max = 120) String name,
			@Email @Size(max = 254) String email) {
	}

	public record StaffInviteView(
			UUID id,
			String token,
			MembershipRole role,
			String name,
			String email,
			Instant expiresAt,
			Instant createdAt) {

		static StaffInviteView of(StaffInvite i) {
			return new StaffInviteView(i.getId(), i.getToken(), i.getRole(), i.getName(), i.getEmail(),
					i.getExpiresAt(), i.getCreatedAt());
		}
	}

	public record RoleRequest(@NotNull MembershipRole role) {
	}

	public record PublicStaffInviteInfo(String academyName, MembershipRole role, String name, String email,
			StaffInvite.View status) {
	}

	/** Conta nova pelo convite. Se o convite tiver e-mail, ele prevalece sobre o informado. */
	public record StaffSignupRequest(
			@NotBlank @Size(max = 120) String name,
			@NotBlank @Email @Size(max = 254) String email,
			@NotBlank @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres") String password) {
	}
}
