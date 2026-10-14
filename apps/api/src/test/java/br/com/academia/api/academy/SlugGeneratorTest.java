package br.com.academia.api.academy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SlugGeneratorTest {

	@Test
	void removesAccentsAndSymbols() {
		assertThat(SlugGenerator.slugify("  Academia São João — Jiu-Jitsu!! ")).isEqualTo("academia-sao-joao-jiu-jitsu");
	}

	@Test
	void fallsBackWhenNameHasNoLetters() {
		assertThat(SlugGenerator.slugify("***")).isEqualTo("academia");
	}

	@Test
	void truncatesLongNames() {
		assertThat(SlugGenerator.slugify("a".repeat(200))).hasSize(60);
	}
}
