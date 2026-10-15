package br.com.academia.api.access.webhook;

import static br.com.academia.api.ApiTestSupport.CTX;
import static br.com.academia.api.ApiTestSupport.auth;
import static br.com.academia.api.ApiTestSupport.json;
import static br.com.academia.api.ApiTestSupport.read;
import static br.com.academia.api.ApiTestSupport.registerOwner;
import static br.com.academia.api.TestcontainersConfiguration.ASAAS_WEBHOOK_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import br.com.academia.api.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AsaasWebhookIntegrationTest {

	private static final String WEBHOOK = CTX + "/webhooks/asaas";
	private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

	@Autowired
	MockMvc mvc;

	@Autowired
	JdbcTemplate jdbc;

	@Test
	void rejectsMissingOrWrongToken() throws Exception {
		String body = event("evt_" + UUID.randomUUID(), "PAYMENT_RECEIVED", "sub_x", LocalDate.now(ZONE));

		mvc.perform(json(post(WEBHOOK), body)).andExpect(status().isUnauthorized());
		mvc.perform(json(post(WEBHOOK), body).header("asaas-access-token", "errado"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void receivedPaymentActivatesTheAcademyForAMonth() throws Exception {
		String owner = registerOwner(mvc);
		String subscription = subscribe(owner);
		LocalDate due = LocalDate.now(ZONE).minusDays(20);
		endTrial(owner);

		mvc.perform(signed(event("evt_" + UUID.randomUUID(), "PAYMENT_RECEIVED", subscription, due)))
				.andExpect(status().isOk());

		assertThat(paidUntil(owner)).isEqualTo(due.plusMonths(1).plusDays(1).atStartOfDay(ZONE).toInstant());
		mvc.perform(auth(get(CTX + "/academy-access"), owner)).andExpect(jsonPath("$.status").value("ACTIVE"));
	}

	@Test
	void redeliveryAndBothPaidEventsGiveTheSameResult() throws Exception {
		String owner = registerOwner(mvc);
		String subscription = subscribe(owner);
		LocalDate due = LocalDate.now(ZONE);
		String eventId = "evt_" + UUID.randomUUID();
		String confirmed = event(eventId, "PAYMENT_CONFIRMED", subscription, due);

		mvc.perform(signed(confirmed)).andExpect(status().isOk());
		Instant first = paidUntil(owner);
		mvc.perform(signed(confirmed)).andExpect(status().isOk());
		mvc.perform(signed(event("evt_" + UUID.randomUUID(), "PAYMENT_RECEIVED", subscription, due)))
				.andExpect(status().isOk());

		assertThat(paidUntil(owner)).isEqualTo(first);
		assertThat(jdbc.queryForObject("select count(*) from payment_events where event_id = ?", Long.class, eventId))
				.isEqualTo(1);
	}

	@Test
	void unknownSubscriptionAndOtherEventsAreOnlyRecorded() throws Exception {
		String eventId = "evt_" + UUID.randomUUID();

		mvc.perform(signed(event(eventId, "PAYMENT_RECEIVED", "sub_desconhecida", LocalDate.now(ZONE))))
				.andExpect(status().isOk());
		mvc.perform(signed(event("evt_" + UUID.randomUUID(), "PAYMENT_CREATED", "sub_desconhecida",
				LocalDate.now(ZONE)))).andExpect(status().isOk());

		assertThat(jdbc.queryForObject("select count(*) from payment_events where event_id = ?", Long.class, eventId))
				.isEqualTo(1);
	}

	@Test
	void malformedEventIsRejected() throws Exception {
		mvc.perform(signed("{\"event\":\"PAYMENT_RECEIVED\"}")).andExpect(status().isBadRequest());
		mvc.perform(signed("não é json")).andExpect(status().isBadRequest());
	}

	private MockHttpServletRequestBuilder signed(String body) {
		return json(post(WEBHOOK), body).header("asaas-access-token", ASAAS_WEBHOOK_TOKEN);
	}

	private static String event(String id, String type, String subscription, LocalDate due) {
		return """
				{"id":"%s","event":"%s","dateCreated":"2026-10-15 10:00:00",
				 "payment":{"object":"payment","id":"pay_%s","subscription":"%s","dueDate":"%s",
				            "status":"RECEIVED","value":100.0,"invoiceUrl":"https://sandbox.asaas.com/i/x"}}
				""".formatted(id, type, UUID.randomUUID(), subscription, due);
	}

	private String subscribe(String owner) throws Exception {
		String subscription = "sub_" + UUID.randomUUID().toString().substring(0, 12);
		jdbc.update("update academies set asaas_subscription_id = ? where id = ?", subscription, academyId(owner));
		return subscription;
	}

	private void endTrial(String owner) throws Exception {
		jdbc.update("update academies set trial_ends_at = now() - interval '30 days' where id = ?", academyId(owner));
	}

	private Instant paidUntil(String owner) throws Exception {
		return jdbc.queryForObject("select paid_until from academies where id = ?", java.sql.Timestamp.class,
				academyId(owner)).toInstant();
	}

	private UUID academyId(String owner) throws Exception {
		return UUID.fromString(read(mvc.perform(auth(get(CTX + "/me"), owner)).andReturn(), "$.academyId"));
	}
}
