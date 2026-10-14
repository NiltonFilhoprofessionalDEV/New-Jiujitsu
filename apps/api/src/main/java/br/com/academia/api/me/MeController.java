package br.com.academia.api.me;

import java.time.Instant;
import java.util.UUID;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.membership.Membership;
import br.com.academia.api.membership.MembershipRepository;
import br.com.academia.api.membership.MembershipRole;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me")
public class MeController {

	private final MembershipRepository memberships;

	public MeController(MembershipRepository memberships) {
		this.memberships = memberships;
	}

	public record MeResponse(
			UUID userId, String name, String email, String language,
			UUID membershipId, MembershipRole role,
			UUID academyId, String academyName, String academySlug, Instant trialEndsAt) {
	}

	@GetMapping
	@Transactional(readOnly = true)
	public MeResponse me() {
		CurrentUser current = CurrentUser.get();
		Membership m = memberships.findActiveById(current.membershipId())
				.orElseThrow(() -> ApiException.unauthorized("INVALID_SESSION", "Sessão expirada. Entre novamente."));
		var user = m.getUser();
		var academy = m.getAcademy();
		return new MeResponse(user.getId(), user.getName(), user.getEmail(), user.getLanguage(),
				m.getId(), m.getRole(),
				academy.getId(), academy.getName(), academy.getSlug(), academy.getTrialEndsAt());
	}
}
