package br.com.academia.api.graduation;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class BeltRuleDtos {

	private BeltRuleDtos() {
	}

	/** {@code custom = false}: a academia ainda usa o valor padrão. */
	public record RuleView(String belt, Integer minMonths, Integer minTrainings, Integer trainingsPerDegree,
			boolean custom) {
	}

	public record RuleInput(
			@NotBlank String belt,
			@Min(0) @Max(240) Integer minMonths,
			@Min(0) @Max(5000) Integer minTrainings,
			@Min(1) @Max(1000) Integer trainingsPerDegree) {
	}

	public record SaveRulesRequest(@NotNull @Size(max = 30) List<@Valid @NotNull RuleInput> rules) {
	}
}
