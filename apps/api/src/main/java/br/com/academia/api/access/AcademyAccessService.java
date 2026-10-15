package br.com.academia.api.access;

import java.time.Clock;
import java.util.UUID;

import br.com.academia.api.academy.Academy;
import br.com.academia.api.academy.AcademyRepository;
import br.com.academia.api.access.AcademyAccessDtos.AccessView;
import br.com.academia.api.common.ApiException;
import br.com.academia.api.config.AppProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Situação de acesso de cada academia, calculada na hora a partir das datas gravadas. */
@Service
public class AcademyAccessService {

	private final AcademyRepository academies;
	private final AppProperties props;
	private final Clock clock;

	public AcademyAccessService(AcademyRepository academies, AppProperties props, Clock clock) {
		this.academies = academies;
		this.props = props;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public Access accessOf(UUID academyId) {
		return evaluate(find(academyId));
	}

	@Transactional(readOnly = true)
	public AccessView view(UUID academyId) {
		Academy academy = find(academyId);
		return AccessView.of(academy, evaluate(academy), props.subscription().monthlyPrice(),
				enforced(), enforced() && props.asaas().enabled());
	}

	/** Desligado, o app fica livre: ninguém é bloqueado e não há cobrança da assinatura. */
	public boolean enforced() {
		return props.subscription().enforced();
	}

	public Access evaluate(Academy academy) {
		return AccessRules.evaluate(academy.getTrialEndsAt(), academy.getPaidUntil(), clock.instant(),
				props.subscription().grace());
	}

	private Academy find(UUID academyId) {
		return academies.findById(academyId)
				.orElseThrow(() -> ApiException.notFound("ACADEMY_NOT_FOUND", "Academia não encontrada"));
	}
}
