package br.com.academia.api.access;

import java.time.Duration;
import java.time.Instant;

/** Regra pura de acesso: teste, perÃ­odo pago e tolerÃ¢ncia. */
public final class AccessRules {

	private static final long SECONDS_PER_DAY = Duration.ofDays(1).toSeconds();

	private AccessRules() {
	}

	public static Access evaluate(Instant trialEndsAt, Instant paidUntil, Instant now, Duration grace) {
		Instant accessUntil = paidUntil != null && paidUntil.isAfter(trialEndsAt) ? paidUntil : trialEndsAt;
		Instant suspendsAt = accessUntil.plus(grace);
		if (paidUntil != null && paidUntil.isAfter(now)) {
			return new Access(AccessStatus.ACTIVE, accessUntil, suspendsAt, daysBetween(now, accessUntil));
		}
		if (trialEndsAt.isAfter(now)) {
			return new Access(AccessStatus.TRIAL, accessUntil, suspendsAt, daysBetween(now, accessUntil));
		}
		if (suspendsAt.isAfter(now)) {
			return new Access(AccessStatus.GRACE, accessUntil, suspendsAt, daysBetween(now, suspendsAt));
		}
		return new Access(AccessStatus.SUSPENDED, accessUntil, suspendsAt, 0);
	}

	/** Dias inteiros atÃ© {@code to}, arredondando para cima: falta 1 segundo = falta 1 dia. */
	private static long daysBetween(Instant from, Instant to) {
		long seconds = Duration.between(from, to).toSeconds();
		return (seconds + SECONDS_PER_DAY - 1) / SECONDS_PER_DAY;
	}
}
