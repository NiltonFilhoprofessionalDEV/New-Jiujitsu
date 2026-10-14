package br.com.academia.api.common;

import java.util.Arrays;
import java.util.Locale;

/** Filtros de query string: valores em minúsculas no frontend, enums em maiúsculas na API. */
public final class EnumParams {

	private EnumParams() {
	}

	public static <E extends Enum<E>> E parse(Class<E> type, String raw, E fallback) {
		if (raw == null || raw.isBlank()) {
			return fallback;
		}
		String name = raw.trim().toUpperCase(Locale.ROOT);
		return Arrays.stream(type.getEnumConstants())
				.filter(e -> e.name().equals(name))
				.findFirst()
				.orElseThrow(() -> ApiException.badRequest("INVALID_FILTER", "Filtro inválido: " + type.getSimpleName()));
	}
}
