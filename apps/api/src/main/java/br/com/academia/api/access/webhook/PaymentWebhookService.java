package br.com.academia.api.access.webhook;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Set;

import br.com.academia.api.academy.AcademyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Aplica os pagamentos confirmados: cada cobrança paga libera um mês a partir do vencimento dela.
 * Gravação do evento e efeito acontecem na mesma transação: se algo falhar, o Asaas reenvia.
 */
@Service
class PaymentWebhookService {

	private static final Logger log = LoggerFactory.getLogger(PaymentWebhookService.class);
	/** Confirmado (cartão) e recebido (Pix/boleto, ou cartão liquidado): os dois liberam o mesmo período. */
	private static final Set<String> PAID_EVENTS = Set.of("PAYMENT_CONFIRMED", "PAYMENT_RECEIVED");

	private final PaymentEventLog eventLog;
	private final AcademyRepository academies;
	private final Clock clock;

	PaymentWebhookService(PaymentEventLog eventLog, AcademyRepository academies, Clock clock) {
		this.eventLog = eventLog;
		this.academies = academies;
		this.clock = clock;
	}

	@Transactional
	void handle(AsaasWebhook event, String rawPayload) {
		if (!eventLog.recordFirstDelivery(event, rawPayload)) {
			return;
		}
		if (!PAID_EVENTS.contains(event.event()) || event.subscriptionId() == null) {
			return;
		}
		Instant paidUntil = paidUntil(event);
		if (paidUntil == null) {
			log.warn("Asaas: evento {} sem vencimento válido; nada aplicado", event.id());
			return;
		}
		academies.findForUpdateByAsaasSubscription(event.subscriptionId()).ifPresentOrElse(
				academy -> academy.payUntil(paidUntil),
				() -> log.info("Asaas: evento {} de assinatura desconhecida; só registrado", event.id()));
	}

	/** Fim do dia, no fuso da plataforma, um mês depois do vencimento da cobrança paga. */
	private Instant paidUntil(AsaasWebhook event) {
		String dueDate = event.payment().dueDate();
		if (dueDate == null) {
			return null;
		}
		try {
			LocalDate due = LocalDate.parse(dueDate);
			return due.plusMonths(1).plusDays(1).atStartOfDay(clock.getZone()).toInstant();
		} catch (DateTimeParseException e) {
			return null;
		}
	}
}
