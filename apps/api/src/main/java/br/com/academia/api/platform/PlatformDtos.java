package br.com.academia.api.platform;

import java.time.Instant;
import java.util.UUID;

import br.com.academia.api.access.Access;
import br.com.academia.api.access.AccessStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class PlatformDtos {

	static final int MAX_GRANT_DAYS = 366;

	private PlatformDtos() {
	}

	public record PlatformAccess(boolean admin) {
	}

	/** Academia como o banco a guarda, com o dono e o total de alunos ativos. */
	record AcademyRow(
			UUID id, String name, String slug, Instant createdAt, Instant trialEndsAt, Instant paidUntil,
			boolean subscribed, String ownerName, String ownerEmail, long activeStudents) {
	}

	public record AdminAcademy(
			UUID id, String name, String slug, Instant createdAt, String ownerName, String ownerEmail,
			long activeStudents, boolean subscribed, Instant trialEndsAt, Instant paidUntil,
			AccessStatus status, Instant accessUntil, Instant suspendsAt, long daysLeft) {

		static AdminAcademy of(AcademyRow row, Access access) {
			return new AdminAcademy(row.id(), row.name(), row.slug(), row.createdAt(), row.ownerName(),
					row.ownerEmail(), row.activeStudents(), row.subscribed(), row.trialEndsAt(), row.paidUntil(),
					access.status(), access.accessUntil(), access.suspendsAt(), access.daysLeft());
		}
	}

	public record GrantRequest(
			@NotNull GrantKind kind,
			@Min(1) @Max(MAX_GRANT_DAYS) int days,
			@Size(max = 200) String note) {
	}
}
