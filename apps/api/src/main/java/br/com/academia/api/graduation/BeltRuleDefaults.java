package br.com.academia.api.graduation;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import br.com.academia.api.student.Belt;

/**
 * Valores iniciais das regras. Azul (24), roxa (18) e marrom (12 meses) seguem o tempo mínimo da IBJJF;
 * branca e faixas infantis não têm tempo mínimo na IBJJF, então usam 12 meses como referência editável.
 * A preta segue a régua fixa de graus da IBJJF ({@link Readiness}) e não é editável aqui.
 */
final class BeltRuleDefaults {

	private static final int REFERENCE_MONTHS = 12;

	/** Faixas com regra configurável, na ordem de exibição. */
	static final List<Belt> EDITABLE = List.of(
			Belt.BRANCA,
			Belt.CINZA_E_BRANCA, Belt.CINZA, Belt.CINZA_E_PRETA,
			Belt.AMARELA_E_BRANCA, Belt.AMARELA, Belt.AMARELA_E_PRETA,
			Belt.LARANJA_E_BRANCA, Belt.LARANJA, Belt.LARANJA_E_PRETA,
			Belt.VERDE_E_BRANCA, Belt.VERDE, Belt.VERDE_E_PRETA,
			Belt.AZUL, Belt.ROXA, Belt.MARROM);

	private static final Map<Belt, RuleValues> DEFAULTS = new EnumMap<>(Belt.class);

	static {
		EDITABLE.forEach(b -> DEFAULTS.put(b, new RuleValues(REFERENCE_MONTHS, null, null)));
		DEFAULTS.put(Belt.AZUL, new RuleValues(24, null, null));
		DEFAULTS.put(Belt.ROXA, new RuleValues(18, null, null));
		DEFAULTS.put(Belt.MARROM, new RuleValues(12, null, null));
	}

	private BeltRuleDefaults() {
	}

	static RuleValues of(Belt belt) {
		return DEFAULTS.getOrDefault(belt, RuleValues.NONE);
	}
}
