package br.com.academia.api.access;

import java.math.BigDecimal;
import java.time.Instant;

import br.com.academia.api.academy.Academy;
import jakarta.validation.constraints.Size;

public final class AcademyAccessDtos {

	private AcademyAccessDtos() {
	}

	/** Assinatura da academia vista pela equipe. */
	public record AccessView(
			AccessStatus status,
			Instant trialEndsAt,
			Instant paidUntil,
			Instant accessUntil,
			Instant suspendsAt,
			long daysLeft,
			BigDecimal monthlyPrice,
			boolean onlinePayment,
			boolean subscribed) {

		static AccessView of(Academy academy, Access access, BigDecimal price, boolean onlinePayment) {
			return new AccessView(access.status(), academy.getTrialEndsAt(), academy.getPaidUntil(),
					access.accessUntil(), access.suspendsAt(), access.daysLeft(), price, onlinePayment,
					academy.getAsaasSubscriptionId() != null);
		}
	}

	/** CPF ou CNPJ de quem paga; obrigatório só na primeira assinatura. */
	public record CheckoutRequest(@Size(max = 20) String cpfCnpj) {
	}

	public record CheckoutResponse(String invoiceUrl) {
	}
}
