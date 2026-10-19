package br.com.academia.api.platform;

import java.time.Instant;
import java.util.UUID;

public final class AcademyInviteDtos {

	private AcademyInviteDtos() {
	}

	public record AcademyInviteView(UUID id, String token, Instant expiresAt, Instant createdAt) {

		static AcademyInviteView of(AcademyCreationInvite invite) {
			return new AcademyInviteView(invite.getId(), invite.getToken(), invite.getExpiresAt(),
					invite.getCreatedAt());
		}
	}

	public record PublicAcademyInviteInfo(AcademyCreationInvite.View status) {
	}
}
