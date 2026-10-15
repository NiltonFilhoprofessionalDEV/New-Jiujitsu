package br.com.academia.api.common;

/**
 * CNPJ numérico ou alfanumérico (Receita Federal, a partir de julho de 2026): 12 caracteres [0-9A-Z]
 * e 2 dígitos verificadores, cada caractere valendo o código ASCII menos 48.
 */
final class Cnpj {

	private static final int LENGTH = 14;
	private static final int[] FIRST_WEIGHTS = { 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };
	private static final int[] SECOND_WEIGHTS = { 6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };

	private Cnpj() {
	}

	/** {@code value} já em maiúsculas e sem máscara. */
	static boolean isValid(String value) {
		if (!value.matches("[0-9A-Z]{12}[0-9]{2}") || value.chars().distinct().count() == 1) {
			return false;
		}
		return checkDigit(value, FIRST_WEIGHTS) == value.charAt(12) - '0'
				&& checkDigit(value, SECOND_WEIGHTS) == value.charAt(13) - '0';
	}

	private static int checkDigit(String value, int[] weights) {
		int sum = 0;
		for (int i = 0; i < weights.length; i++) {
			sum += (value.charAt(i) - '0') * weights[i];
		}
		int rest = sum % 11;
		return rest < 2 ? 0 : 11 - rest;
	}
}
