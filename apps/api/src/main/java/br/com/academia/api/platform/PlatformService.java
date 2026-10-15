package br.com.academia.api.platform;

import static br.com.academia.api.common.TextNormalizer.blankToNull;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import br.com.academia.api.academy.Academy;
import br.com.academia.api.academy.AcademyRepository;
import br.com.academia.api.access.AccessRules;
import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.config.AppProperties;
import br.com.academia.api.platform.PlatformDtos.AcademyRow;
import br.com.academia.api.platform.PlatformDtos.AdminAcademy;
import br.com.academia.api.platform.PlatformDtos.GrantRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlatformService {

	private final PlatformAdmins admins;
	private final PlatformAcademyQuery query;
	private final AcademyRepository academies;
	private final AccessGrantRepository grants;
	private final AppProperties props;
	private final Clock clock;

	public PlatformService(PlatformAdmins admins, PlatformAcademyQuery query, AcademyRepository academies,
			AccessGrantRepository grants, AppProperties props, Clock clock) {
		this.admins = admins;
		this.query = query;
		this.academies = academies;
		this.grants = grants;
		this.props = props;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public List<AdminAcademy> academies(CurrentUser me) {
		admins.requireAdmin(me.userId());
		return query.all().stream().map(this::toView).toList();
	}

	/** Teste estendido ou pagamento manual; os dias contam do fim atual ou de agora, o que for maior. */
	@Transactional
	public AdminAcademy grant(CurrentUser me, UUID academyId, GrantRequest req) {
		admins.requireAdmin(me.userId());
		Academy academy = academies.findById(academyId)
				.orElseThrow(() -> ApiException.notFound("ACADEMY_NOT_FOUND", "Academia não encontrada"));
		Duration extra = Duration.ofDays(req.days());
		switch (req.kind()) {
			case TRIAL_EXTENSION -> academy.extendTrial(extra, clock.instant());
			case MANUAL_PAYMENT -> academy.extendPaidPeriod(extra, clock.instant());
		}
		academies.saveAndFlush(academy);
		grants.save(new AccessGrant(academyId, req.kind(), req.days(), blankToNull(req.note()), me.userId()));
		return query.find(academyId).map(this::toView).orElseThrow();
	}

	private AdminAcademy toView(AcademyRow row) {
		return AdminAcademy.of(row, AccessRules.evaluate(row.trialEndsAt(), row.paidUntil(), clock.instant(),
				props.subscription().grace()));
	}
}
