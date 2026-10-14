package br.com.academia.api.billing;

/** Valor cobrado de uma mensalidade, em centavos, a partir do preço do plano e do desconto do aluno. */
final class Pricing {

	private static final long PERCENT_BASE = 100;

	private Pricing() {
	}

	/** Percentual arredonda o desconto meio-centavo para cima; desconto maior que o preço zera a cobrança. */
	static int amountCents(int priceCents, DiscountType type, Integer value) {
		if (type == null || value == null) {
			return priceCents;
		}
		long discount = switch (type) {
			case PERCENT -> (priceCents * (long) value + PERCENT_BASE / 2) / PERCENT_BASE;
			case FIXED -> value;
		};
		return (int) Math.max(0, priceCents - discount);
	}
}
