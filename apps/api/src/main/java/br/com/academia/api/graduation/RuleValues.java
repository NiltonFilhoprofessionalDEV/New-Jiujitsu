package br.com.academia.api.graduation;

/**
 * Critérios de uma faixa; {@code null} = critério não exigido. Faixa sem nenhum critério de troca
 * não gera aviso de "pronto" (evita listar a academia inteira por falta de configuração).
 */
record RuleValues(Integer minMonths, Integer minTrainings, Integer trainingsPerDegree) {

	static final RuleValues NONE = new RuleValues(null, null, null);

	boolean tracksBelt() {
		return minMonths != null || minTrainings != null;
	}
}
