package br.com.academia.api.access.asaas;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Contratos da API v3 do Asaas usados aqui (só os campos necessários). Datas em ISO {@code yyyy-MM-dd}. */
public final class AsaasDtos {

	private AsaasDtos() {
	}

	public record NewCustomer(String name, String cpfCnpj, String email, String externalReference) {
	}

	public record NewSubscription(
			String customer, String billingType, BigDecimal value, String nextDueDate, String cycle,
			String description, String externalReference) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Created(String id) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Payment(String id, String status, String dueDate, String invoiceUrl) {

		public boolean isOpen() {
			return "PENDING".equals(status) || "OVERDUE".equals(status);
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record PaymentPage(List<Payment> data) {
	}
}
