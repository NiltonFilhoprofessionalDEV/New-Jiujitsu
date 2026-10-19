package br.com.academia.api.common;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Nome de pessoa no padrão de documento: "maria DA silva" vira "Maria da Silva". */
public final class PersonName {

	private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
	/** Ligações que ficam minúsculas no meio do nome e não contam como sobrenome. */
	private static final Set<String> PARTICLES = Set.of("da", "das", "de", "do", "dos", "e");
	private static final int MIN_NAMES = 2;
	/** Inclui o espaço não separável (U+00A0), comum em nome colado do WhatsApp. */
	private static final String SPACES = "[\\s\\u00A0]+";

	private PersonName() {
	}

	public static String format(String raw) {
		String collapsed = raw == null ? "" : raw.replaceAll(SPACES, " ").trim();
		if (collapsed.isEmpty()) {
			return collapsed;
		}
		String[] words = collapsed.toLowerCase(PT_BR).split(" ");
		StringBuilder out = new StringBuilder(collapsed.length());
		for (int i = 0; i < words.length; i++) {
			if (i > 0) {
				out.append(' ');
			}
			out.append(i > 0 && PARTICLES.contains(words[i]) ? words[i] : capitalize(words[i]));
		}
		return out.toString();
	}

	/**
	 * Telefone no lugar do nome: só dígitos e pontuação, ou um telefone com DDD grudado no texto.
	 * "62991948360" e "Maria 62991948360" caem aqui; "Maria Souza" e "Joana (mãe)" não.
	 */
	public static boolean isPhoneLike(String raw) {
		if (raw == null || raw.isBlank()) {
			return false;
		}
		boolean hasLetter = raw.codePoints().anyMatch(Character::isLetter);
		return !hasLetter || Phone.isValid(raw);
	}

	public static FieldError phoneInName(String field) {
		return new FieldError(field, "PERSON_NAME_REQUIRED", "Informe o nome da pessoa, não o telefone");
	}

	public static void rejectPhone(String raw, String field) {
		if (isPhoneLike(raw)) {
			throw ApiException.invalidFields(List.of(phoneInName(field)));
		}
	}

	/** Tem nome e sobrenome? As ligações ("da", "dos"...) não contam como sobrenome. */
	public static boolean isFull(String name) {
		long names = Arrays.stream(format(name).split(" "))
				.filter(word -> !word.isEmpty() && !PARTICLES.contains(word.toLowerCase(PT_BR)))
				.count();
		return names >= MIN_NAMES;
	}

	/** Maiúscula no começo e depois de hífen ou apóstrofo: "ana-clara" e "d'ávila" viram "Ana-Clara" e "D'Ávila". */
	private static String capitalize(String word) {
		StringBuilder out = new StringBuilder(word.length());
		boolean upperNext = true;
		for (int i = 0; i < word.length(); ) {
			int codePoint = word.codePointAt(i);
			out.appendCodePoint(upperNext ? Character.toTitleCase(codePoint) : codePoint);
			upperNext = codePoint == '-' || codePoint == '\'' || codePoint == '’';
			i += Character.charCount(codePoint);
		}
		return out.toString();
	}
}
