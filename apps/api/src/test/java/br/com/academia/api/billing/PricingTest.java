package br.com.academia.api.billing;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PricingTest {

	@Test
	void withoutDiscountChargesFullPrice() {
		assertThat(Pricing.amountCents(15_000, null, null)).isEqualTo(15_000);
	}

	@Test
	void percentRoundsHalfCentOfDiscountUp() {
		assertThat(Pricing.amountCents(15_000, DiscountType.PERCENT, 10)).isEqualTo(13_500);
		assertThat(Pricing.amountCents(999, DiscountType.PERCENT, 15)).isEqualTo(849);
		assertThat(Pricing.amountCents(15_000, DiscountType.PERCENT, 100)).isZero();
	}

	@Test
	void fixedNeverGoesBelowZero() {
		assertThat(Pricing.amountCents(15_000, DiscountType.FIXED, 2_000)).isEqualTo(13_000);
		assertThat(Pricing.amountCents(15_000, DiscountType.FIXED, 20_000)).isZero();
	}

	@Test
	void percentOnMaxPrice() {
		assertThat(Pricing.amountCents(PlanDtos.MAX_PRICE_CENTS, DiscountType.PERCENT, 33)).isEqualTo(6_700_000);
	}
}
