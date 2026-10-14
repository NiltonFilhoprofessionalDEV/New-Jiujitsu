package br.com.academia.api.student;

import java.util.Arrays;

import br.com.academia.api.common.ApiException;

/** Faixas da IBJJF, identificadas pelo mesmo slug usado na API e no banco. */
public enum Belt {
	BRANCA("branca", 0, 4),
	CINZA_E_BRANCA("cinza-e-branca", 0, 4),
	CINZA("cinza", 0, 4),
	CINZA_E_PRETA("cinza-e-preta", 0, 4),
	AMARELA_E_BRANCA("amarela-e-branca", 0, 4),
	AMARELA("amarela", 0, 4),
	AMARELA_E_PRETA("amarela-e-preta", 0, 4),
	LARANJA_E_BRANCA("laranja-e-branca", 0, 4),
	LARANJA("laranja", 0, 4),
	LARANJA_E_PRETA("laranja-e-preta", 0, 4),
	VERDE_E_BRANCA("verde-e-branca", 0, 4),
	VERDE("verde", 0, 4),
	VERDE_E_PRETA("verde-e-preta", 0, 4),
	AZUL("azul", 0, 4),
	ROXA("roxa", 0, 4),
	MARROM("marrom", 0, 4),
	PRETA("preta", 0, 6),
	VERMELHA_E_PRETA("vermelha-e-preta", 7, 7),
	VERMELHA_E_BRANCA("vermelha-e-branca", 8, 8),
	VERMELHA("vermelha", 9, 10);

	private final String slug;
	private final int minDegrees;
	private final int maxDegrees;

	Belt(String slug, int minDegrees, int maxDegrees) {
		this.slug = slug;
		this.minDegrees = minDegrees;
		this.maxDegrees = maxDegrees;
	}

	public String slug() {
		return slug;
	}

	public int minDegrees() {
		return minDegrees;
	}

	public int maxDegrees() {
		return maxDegrees;
	}

	public static Belt fromSlug(String slug) {
		return Arrays.stream(values())
				.filter(b -> b.slug.equals(slug))
				.findFirst()
				.orElseThrow(() -> ApiException.badRequest("INVALID_BELT", "Faixa inválida"));
	}

	public void requireValidDegrees(int degrees) {
		if (degrees < minDegrees || degrees > maxDegrees) {
			throw ApiException.badRequest("INVALID_DEGREES",
					"Grau inválido para a faixa (permitido de %d a %d)".formatted(minDegrees, maxDegrees));
		}
	}
}
