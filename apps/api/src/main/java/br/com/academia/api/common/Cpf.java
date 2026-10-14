package br.com.academia.api.common;

/** CPF brasileiro: aceita com ou sem máscara e valida os dígitos verificadores. */
public final class Cpf {

	private static final int LENGTH = 11;

	private Cpf() {
	}

	/** Retorna só os dígitos, ou {@code null} quando vazio. Lança 400 se o CPF for inválido. */
	public static String normalize(String raw) {
		String digits = TextNormalizer.digits(raw);
		if (digits.isEmpty()) {
			return null;
		}
		if (!isValid(digits)) {
			throw ApiException.badRequest("INVALID_CPF", "CPF inválido");
		}
		return digits;
	}

	static boolean isValid(String digits) {
		if (digits.length() != LENGTH || digits.chars().distinct().count() == 1) {
			return false;
		}
		return checkDigit(digits, 9) == digits.charAt(9) - '0' && checkDigit(digits, 10) == digits.charAt(10) - '0';
	}

	private static int checkDigit(String digits, int length) {
		int sum = 0;
		for (int i = 0; i < length; i++) {
			sum += (digits.charAt(i) - '0') * (length + 1 - i);
		}
		int rest = (sum * 10) % 11;
		return rest == 10 ? 0 : rest;
	}
}
