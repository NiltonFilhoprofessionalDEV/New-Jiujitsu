package br.com.academia.api.common;

import java.text.Normalizer;
import java.util.Locale;

/** Forma comparável de textos digitados: minúsculas, sem acentos e com espaços normalizados. */
public final class TextNormalizer {

	private TextNormalizer() {
	}

	public static String fold(String text) {
		if (text == null) {
			return "";
		}
		String ascii = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
		return ascii.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
	}

	public static String digits(String text) {
		return text == null ? "" : text.replaceAll("\\D", "");
	}

	/** Escapa os curingas do LIKE; usar com o caractere de escape {@code \}. */
	public static String likeContains(String folded) {
		return "%" + folded.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
	}

	public static String blankToNull(String text) {
		return text == null || text.isBlank() ? null : text.trim();
	}
}
