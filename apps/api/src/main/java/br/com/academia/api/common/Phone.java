package br.com.academia.api.common;

/** Telefone brasileiro digitado livremente (com ou sem máscara e +55); é guardado como foi digitado. */
public final class Phone {

	/** DDD + 8 dígitos (fixo). */
	private static final int MIN_DIGITS = 10;
	/** 55 + DDD + 9 dígitos (celular com código do país). */
	private static final int MAX_DIGITS = 13;

	private Phone() {
	}

	/** Tem DDD e não tem dígitos demais. */
	public static boolean isValid(String text) {
		int digits = TextNormalizer.digits(text == null ? "" : text).length();
		return digits >= MIN_DIGITS && digits <= MAX_DIGITS;
	}
}
