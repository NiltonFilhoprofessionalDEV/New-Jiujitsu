package br.com.academia.api.access;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

class AccessRulesTest {

	private static final Duration GRACE = Duration.ofDays(5);
	private static final Instant TRIAL_END = Instant.parse("2026-11-01T12:00:00Z");

	private static Access evaluate(Instant paidUntil, Instant now) {
		return AccessRules.evaluate(TRIAL_END, paidUntil, now, GRACE);
	}

	@Test
	void trialCoversUntilItsLastSecond() {
		Access access = evaluate(null, TRIAL_END.minusSeconds(1));

		assertThat(access.status()).isEqualTo(AccessStatus.TRIAL);
		assertThat(access.accessUntil()).isEqualTo(TRIAL_END);
		assertThat(access.daysLeft()).isEqualTo(1);
	}

	@Test
	void freshTrialCountsWholeDays() {
		assertThat(evaluate(null, TRIAL_END.minus(Duration.ofDays(14))).daysLeft()).isEqualTo(14);
	}

	@Test
	void graceStartsWhenAccessEnds() {
		Access access = evaluate(null, TRIAL_END);

		assertThat(access.status()).isEqualTo(AccessStatus.GRACE);
		assertThat(access.suspendsAt()).isEqualTo(TRIAL_END.plus(GRACE));
		assertThat(access.daysLeft()).isEqualTo(5);
	}

	@Test
	void suspendedAfterGrace() {
		Access access = evaluate(null, TRIAL_END.plus(GRACE));

		assertThat(access.status()).isEqualTo(AccessStatus.SUSPENDED);
		assertThat(access.daysLeft()).isZero();
	}

	@Test
	void paymentActivatesEvenAfterTheTrialExpired() {
		Instant now = TRIAL_END.plus(Duration.ofDays(30));
		Access access = evaluate(now.plus(Duration.ofDays(10)), now);

		assertThat(access.status()).isEqualTo(AccessStatus.ACTIVE);
		assertThat(access.daysLeft()).isEqualTo(10);
	}

	@Test
	void oldPaymentDoesNotShortenTheTrial() {
		Instant now = TRIAL_END.minus(Duration.ofDays(3));
		Access access = evaluate(TRIAL_END.minus(Duration.ofDays(20)), now);

		assertThat(access.status()).isEqualTo(AccessStatus.TRIAL);
		assertThat(access.accessUntil()).isEqualTo(TRIAL_END);
	}

	@Test
	void expiredPaymentFallsIntoGrace() {
		Instant paidUntil = TRIAL_END.plus(Duration.ofDays(30));
		Access access = evaluate(paidUntil, paidUntil.plus(Duration.ofDays(1)));

		assertThat(access.status()).isEqualTo(AccessStatus.GRACE);
		assertThat(access.accessUntil()).isEqualTo(paidUntil);
		assertThat(access.daysLeft()).isEqualTo(4);
	}
}
