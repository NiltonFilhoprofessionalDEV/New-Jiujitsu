package br.com.academia.api.access.webhook;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Registro dos eventos recebidos; o id do evento garante que cada um seja processado uma vez só. */
@Repository
class PaymentEventLog {

	private final JdbcClient jdbc;

	PaymentEventLog(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	/** Grava o evento; {@code false} quando ele já tinha chegado antes (reenvio do Asaas). */
	boolean recordFirstDelivery(AsaasWebhook event, String rawPayload) {
		int inserted = jdbc.sql("""
				insert into payment_events (event_id, event, payment_id, subscription_id, payload)
				values (:eventId, :event, :paymentId, :subscriptionId, cast(:payload as jsonb))
				on conflict (event_id) do nothing
				""")
				.param("eventId", event.id())
				.param("event", event.event())
				.param("paymentId", event.paymentId())
				.param("subscriptionId", event.subscriptionId())
				.param("payload", rawPayload)
				.update();
		return inserted == 1;
	}
}
