package br.com.academia.api.team;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.membership.Membership;
import br.com.academia.api.membership.MembershipRepository;
import br.com.academia.api.membership.MembershipRole;
import br.com.academia.api.membership.StaffService;
import br.com.academia.api.team.TeamDtos.TeamMember;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Equipe: o dono gerencia todos; o co-gestor só professores. O dono não é alterado nem removido por aqui.
 * Mudanças de papel e remoções valem no próximo token de acesso (no máximo o TTL do token atual).
 */
@Service
public class TeamService {

	/** Papéis que entram por convite e podem ser trocados entre si. */
	static final Set<MembershipRole> INVITABLE = EnumSet.of(MembershipRole.INSTRUCTOR, MembershipRole.CO_MANAGER);

	private final MembershipRepository memberships;

	public TeamService(MembershipRepository memberships) {
		this.memberships = memberships;
	}

	@Transactional(readOnly = true)
	public List<TeamMember> list(CurrentUser me) {
		return memberships.findActiveInAcademyByRoles(me.academyId(), StaffService.STAFF_ROLES).stream()
				.map(m -> TeamMember.of(m, me.membershipId()))
				.toList();
	}

	@Transactional
	public TeamMember changeRole(CurrentUser me, UUID membershipId, MembershipRole role) {
		requireOwner(me);
		requireInvitable(role);
		Membership target = requireManageable(me, membershipId);
		target.changeRole(role);
		return TeamMember.of(target, me.membershipId());
	}

	/** Apaga o vínculo: aulas, chamadas e graduações registradas por ele ficam sem autor (FK com set null). */
	@Transactional
	public void remove(CurrentUser me, UUID membershipId) {
		if (membershipId.equals(me.membershipId())) {
			throw ApiException.conflict("CANNOT_REMOVE_SELF", "Você não pode remover a si mesmo da equipe");
		}
		memberships.delete(requireManageable(me, membershipId));
	}

	static void requireOwner(CurrentUser me) {
		if (me.role() != MembershipRole.OWNER) {
			throw ApiException.forbidden("OWNER_ONLY", "Somente o dono da academia pode fazer isso");
		}
	}

	static void requireInvitable(MembershipRole role) {
		if (!INVITABLE.contains(role)) {
			throw ApiException.badRequest("INVALID_ROLE", "Escolha professor ou co-gestor");
		}
	}

	private Membership requireManageable(CurrentUser me, UUID membershipId) {
		Membership target = memberships.findActiveInAcademy(membershipId, me.academyId())
				.filter(m -> INVITABLE.contains(m.getRole()))
				.orElseThrow(() -> ApiException.notFound("MEMBER_NOT_FOUND", "Membro da equipe não encontrado"));
		if (target.getRole() == MembershipRole.CO_MANAGER) {
			requireOwner(me);
		}
		return target;
	}
}
