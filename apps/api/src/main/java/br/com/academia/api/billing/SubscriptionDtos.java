package br.com.academia.api.billing;

import java.time.LocalDate;
import java.util.UUID;

import br.com.academia.api.student.StudentEnums.Status;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class SubscriptionDtos {

	private SubscriptionDtos() {
	}

	/** {@code dueDay} vazio usa o do plano; tipo e valor do desconto vêm juntos ou não vêm. */
	public record TermsRequest(
			@NotNull UUID planId,
			@Min(1) @Max(28) Integer dueDay,
			DiscountType discountType,
			@Min(0) @Max(PlanDtos.MAX_PRICE_CENTS) Integer discountValue,
			@Size(max = 120) String discountReason) {
	}

	/** {@code firstDueDate} vazio: o próximo dia de vencimento a partir de hoje. */
	public record LinkRequest(@NotNull UUID studentId, @NotNull @Valid TermsRequest terms, LocalDate firstDueDate) {
	}

	public record StudentRef(UUID id, String name, String nickname, Status status) {
	}

	public record PlanRef(UUID id, String name, int priceCents, int intervalMonths) {
	}

	/**
	 * {@code amountCents}: o que a próxima fatura vai cobrar, já com desconto. {@code nextDue}: o próximo
	 * vencimento no prazo, emitido ou não. {@code overdueSince}/{@code overdueCents}: faturas vencidas em aberto.
	 */
	public record SubscriptionView(
			UUID id,
			StudentRef student,
			PlanRef plan,
			int dueDay,
			DiscountType discountType,
			Integer discountValue,
			String discountReason,
			int amountCents,
			LocalDate nextDue,
			LocalDate startedOn,
			LocalDate overdueSince,
			long overdueCents) {
	}
}
