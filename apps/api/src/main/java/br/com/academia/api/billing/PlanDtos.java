package br.com.academia.api.billing;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class PlanDtos {

	/** Teto de um plano (R$ 100.000,00), igual ao check do banco. */
	static final int MAX_PRICE_CENTS = 10_000_000;

	private PlanDtos() {
	}

	/** Dia de vencimento até 28 para existir em todos os meses. */
	public record PlanRequest(
			@NotBlank @Size(max = 80) String name,
			@Size(max = 500) String description,
			@NotNull @Min(0) @Max(MAX_PRICE_CENTS) Integer priceCents,
			@NotNull @Min(1) @Max(12) Integer intervalMonths,
			@NotNull @Min(1) @Max(28) Integer dueDay,
			@NotNull Boolean active,
			@NotNull Boolean isDefault,
			@NotNull Boolean isKidsDefault) {
	}

	/** {@code subscribers}: mensalidades ativas neste plano. */
	public record PlanView(
			UUID id,
			String name,
			String description,
			int priceCents,
			int intervalMonths,
			int dueDay,
			boolean active,
			boolean isDefault,
			boolean isKidsDefault,
			long subscribers) {

		static PlanView of(Plan p, long subscribers) {
			return new PlanView(p.getId(), p.getName(), p.getDescription(), p.getPriceCents(), p.getIntervalMonths(),
					p.getDueDay(), p.isActive(), p.isDefault(), p.isKidsDefault(), subscribers);
		}
	}
}
