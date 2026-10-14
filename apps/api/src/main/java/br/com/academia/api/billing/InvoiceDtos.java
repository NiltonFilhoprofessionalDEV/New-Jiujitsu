package br.com.academia.api.billing;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.UUID;

import br.com.academia.api.billing.Invoice.PaymentMethod;
import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.EnumParams;
import br.com.academia.api.common.PageResponse;
import jakarta.validation.constraints.NotNull;

public final class InvoiceDtos {

	private InvoiceDtos() {
	}

	/** Situação exibida: {@code OVERDUE} é uma fatura {@code OPEN} com vencimento antes de hoje. */
	public enum Situation {
		OPEN, OVERDUE, PAID, CANCELED
	}

	public enum SituationFilter {
		ALL, OPEN, OVERDUE, PAID, CANCELED
	}

	/** {@code month} vazio: todos os vencimentos. {@code q}: busca pelo nome do aluno. */
	public record InvoiceFilter(SituationFilter situation, YearMonth month, String q) {

		public static InvoiceFilter of(String situation, String month, String q) {
			return new InvoiceFilter(
					EnumParams.parse(SituationFilter.class, situation, SituationFilter.ALL),
					parseMonth(month),
					q == null ? "" : q.trim());
		}

		private static YearMonth parseMonth(String raw) {
			if (raw == null || raw.isBlank()) {
				return null;
			}
			try {
				return YearMonth.parse(raw.trim());
			} catch (DateTimeParseException e) {
				throw ApiException.badRequest("INVALID_FILTER", "Mês inválido, use AAAA-MM");
			}
		}
	}

	public record InvoiceRow(
			UUID id,
			UUID studentId,
			String studentName,
			String description,
			int amountCents,
			LocalDate dueDate,
			Situation situation,
			LocalDate paidOn,
			PaymentMethod paymentMethod) {
	}

	public record Bucket(long count, long cents) {
	}

	/** Somas do mês e da busca escolhidos, sem o filtro de situação; canceladas ficam de fora. */
	public record Totals(Bucket open, Bucket overdue, Bucket paid) {
	}

	/** Inadimplência da academia inteira, independente dos filtros. */
	public record OverdueSummary(long students, long cents) {
	}

	public record InvoiceList(PageResponse<InvoiceRow> page, Totals totals, OverdueSummary overdue) {
	}

	public record PayRequest(@NotNull LocalDate paidOn, @NotNull PaymentMethod method) {
	}
}
