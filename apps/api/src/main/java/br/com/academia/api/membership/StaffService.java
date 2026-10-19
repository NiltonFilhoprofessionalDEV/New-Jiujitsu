package br.com.academia.api.membership;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.academia.api.common.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Equipe da academia (gestores e professores): quem pode dar aula. */
@Service
public class StaffService {

	public static final Set<MembershipRole> STAFF_ROLES =
			Set.of(MembershipRole.OWNER, MembershipRole.CO_MANAGER, MembershipRole.INSTRUCTOR);

	public record StaffMember(UUID membershipId, String name, MembershipRole role) {
	}

	private final MembershipRepository memberships;

	public StaffService(MembershipRepository memberships) {
		this.memberships = memberships;
	}

	@Transactional(readOnly = true)
	public List<StaffMember> list(UUID academyId) {
		return memberships.findActiveInAcademyByRoles(academyId, STAFF_ROLES).stream()
				.filter(m -> !m.isPlatformSupport())
				.map(m -> new StaffMember(m.getId(), m.getUser().getName(), m.getRole()))
				.toList();
	}

	/** Nome de cada membro da equipe por id de vínculo, para montar listas sem uma consulta por item. */
	@Transactional(readOnly = true)
	public Map<UUID, String> namesById(UUID academyId) {
		return list(academyId).stream().collect(Collectors.toMap(StaffMember::membershipId, StaffMember::name));
	}

	public void requireStaff(UUID academyId, UUID membershipId) {
		boolean found = list(academyId).stream().anyMatch(s -> s.membershipId().equals(membershipId));
		if (!found) {
			throw ApiException.badRequest("INVALID_INSTRUCTOR", "Professor não encontrado na equipe da academia");
		}
	}
}
