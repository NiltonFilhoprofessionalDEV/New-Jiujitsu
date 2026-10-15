package br.com.academia.api.access.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Corpo do webhook de cobranças do Asaas (só os campos usados). */
@JsonIgnoreProperties(ignoreUnknown = true)
record AsaasWebhook(String id, String event, WebhookPayment payment) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	record WebhookPayment(String id, String subscription, String dueDate, String status) {
	}

	String paymentId() {
		return payment == null ? null : payment.id();
	}

	String subscriptionId() {
		return payment == null ? null : payment.subscription();
	}
}
