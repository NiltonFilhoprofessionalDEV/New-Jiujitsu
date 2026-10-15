package br.com.academia.api.common;

import java.util.Locale;

/** CPF ou CNPJ de quem paga: aceita com ou sem máscara e valida os dígitos verificadores. */
public final class TaxId {

	private static final int CPF_LENGTH = 11;

	private TaxId() {
	}

	/** Retorna sem máscara e em maiúsculas, ou {@code null} quando vazio. Lança 400 se for inválido. */
	public static String normalize(String raw) {
		String value = raw == null ? "" : raw.replaceAll("[^0-9A-Za-z]", "").toUpperCase(Locale.ROOT);
		if (value.isEmpty()) {
			return null;
		}
		boolean valid = value.length() == CPF_LENGTH ? value.matches("\\d+") && Cpf.isValid(value) : Cnpj.isValid(value);
		if (!valid) {
			throw ApiException.badRequest("INVALID_TAX_ID", "CPF ou CNPJ inválido");
		}
		return value;
	}
}
