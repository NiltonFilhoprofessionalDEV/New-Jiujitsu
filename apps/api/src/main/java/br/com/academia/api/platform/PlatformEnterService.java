package br.com.academia.api.platform;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import br.com.academia.api.academy.Academy;
import br.com.academia.api.academy.AcademyRepository;
import br.com.academia.api.auth.AuthDtos.IssuedSession;
import br.com.academia.api.auth.AuthService;
import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.membership.Membership;
import br.com.academia.api.membership.MembershipRepository;
import br.com.academia.api.membership.MembershipRole;
import br.com.academia.api.membership.StaffService;
import br.com.academia.api.user.User;
import br.com.academia.api.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Abre a academia para o admin da plataforma, com um vínculo de dono que a equipe não vê. */
@Service
public class PlatformEnterService {

	private static final Comparator<Membership> BY_AUTHORITY = Comparator.comparingInt(PlatformEnterService::rank);

	private final PlatformAdmins admins;
	private final AcademyRepository academies;
	private final UserRepository users;
	private final MembershipRepository memberships;
	private final AuthService auth;

	public PlatformEnterService(PlatformAdmins admins, AcademyRepository academies, UserRepository users,
			MembershipRepository memberships, AuthService auth) {
		this.admins = admins;
		this.academies = academies;
		this.users = users;
		this.memberships = memberships;
		this.auth = auth;
	}

	@Transactional
	public IssuedSession enter(CurrentUser me, UUID academyId, String refreshToken) {
		admins.requireAdmin(me.userId());
		Academy academy = academies.findById(academyId)
				.orElseThrow(() -> ApiException.notFound("ACADEMY_NOT_FOUND", "Academia não encontrada"));
		User user = users.findById(me.userId())
				.orElseThrow(() -> ApiException.unauthorized("INVALID_SESSION", "Sessão expirada. Entre novamente."));
		Membership target = resolve(user, academy);
		return auth.switchAcademy(user.getId(), target.getId(), refreshToken);
	}

	private Membership resolve(User user, Academy academy) {
		List<Membership> existing = memberships.findByUserAndAcademy(user.getId(), academy.getId());
		return existing.stream()
				.filter(m -> m.isActive() && !m.isPlatformSupport() && StaffService.STAFF_ROLES.contains(m.getRole()))
				.max(BY_AUTHORITY)
				.orElseGet(() -> supportOf(user, academy, existing));
	}

	private Membership supportOf(User user, Academy academy, List<Membership> existing) {
		return existing.stream().filter(Membership::isPlatformSupport).findFirst().map(this::reactivate)
				.orElseGet(() -> memberships.save(Membership.platformSupport(user, academy)));
	}

	private Membership reactivate(Membership membership) {
		if (!membership.isActive()) {
			membership.activate();
		}
		return membership;
	}

	private static int rank(Membership membership) {
		MembershipRole role = membership.getRole();
		if (role == MembershipRole.OWNER) return 3;
		if (role == MembershipRole.CO_MANAGER) return 2;
		return 1;
	}
}
