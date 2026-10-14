package br.com.academia.api.academy;

import java.util.concurrent.ThreadLocalRandom;

import br.com.academia.api.common.TextNormalizer;
import org.springframework.stereotype.Component;

@Component
public class SlugGenerator {

	private static final int MAX_BASE_LENGTH = 60;
	private static final int MAX_ATTEMPTS = 20;

	private final AcademyRepository academies;

	public SlugGenerator(AcademyRepository academies) {
		this.academies = academies;
	}

	public String uniqueSlug(String name) {
		String base = slugify(name);
		if (!academies.existsBySlug(base)) {
			return base;
		}
		for (int i = 0; i < MAX_ATTEMPTS; i++) {
			String candidate = base + "-" + ThreadLocalRandom.current().nextInt(1000, 9999);
			if (!academies.existsBySlug(candidate)) {
				return candidate;
			}
		}
		throw new IllegalStateException("Não foi possível gerar um identificador único para a academia");
	}

	static String slugify(String name) {
		String slug = TextNormalizer.fold(name).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
		if (slug.isEmpty()) {
			slug = "academia";
		}
		return slug.length() > MAX_BASE_LENGTH ? slug.substring(0, MAX_BASE_LENGTH) : slug;
	}
}
