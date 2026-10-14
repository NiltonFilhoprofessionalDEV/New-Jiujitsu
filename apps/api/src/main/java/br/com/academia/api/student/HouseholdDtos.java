package br.com.academia.api.student;

import java.util.List;
import java.util.UUID;

import br.com.academia.api.student.StudentDtos.StudentListItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class HouseholdDtos {

	/** Limite de membros por requisição de criação, para conter payloads abusivos. */
	static final int MAX_MEMBERS = 30;

	private HouseholdDtos() {
	}

	public record CreateHouseholdRequest(
			@NotBlank @Size(max = 120) String name,
			UUID responsibleStudentId,
			@Size(max = MAX_MEMBERS) List<@NotNull UUID> memberIds) {
	}

	public record RenameHouseholdRequest(@NotBlank @Size(max = 120) String name) {
	}

	public record StudentIdRequest(@NotNull UUID studentId) {
	}

	public record StudentRef(UUID id, String name) {
	}

	public record HouseholdSummary(UUID id, String name, StudentRef responsible, long memberCount) {
	}

	public record HouseholdDetail(UUID id, String name, UUID responsibleStudentId, List<StudentListItem> members) {
	}
}
