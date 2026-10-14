package br.com.academia.api.billing;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Roda todo dia e também ao subir a API, para recuperar um dia em que o servidor esteve fora.
 * Rodar duas vezes não duplica: a mensalidade é travada e a fatura é única por vencimento.
 */
@Component
class BillingScheduler {

	private static final Logger log = LoggerFactory.getLogger(BillingScheduler.class);

	private final SubscriptionRepository subscriptions;
	private final BillingCycle cycle;
	private final Clock clock;

	BillingScheduler(SubscriptionRepository subscriptions, BillingCycle cycle, Clock clock) {
		this.subscriptions = subscriptions;
		this.cycle = cycle;
		this.clock = clock;
	}

	@EventListener(ApplicationReadyEvent.class)
	@Scheduled(cron = "${app.billing.cron}", zone = "${app.time-zone}")
	void run() {
		LocalDate horizon = LocalDate.now(clock).plusDays(BillingCycle.ISSUE_DAYS_AHEAD);
		int issued = 0;
		int failed = 0;
		for (UUID id : subscriptions.findDueIds(horizon)) {
			// Isola a falha de uma mensalidade para não interromper a emissão das demais.
			try {
				issued += cycle.issueDue(id);
			} catch (RuntimeException e) {
				failed++;
				log.error("Falha ao emitir faturas da mensalidade {}", id, e);
			}
		}
		log.info("Emissão de mensalidades: {} fatura(s) emitida(s), {} falha(s)", issued, failed);
	}
}
