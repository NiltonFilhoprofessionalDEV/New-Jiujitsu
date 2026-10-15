package br.com.academia.api.access.webhook;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.config.AppProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Webhook de cobranças do Asaas. Público na segurança da API, mas só aceita quem envia o token
 * combinado no cabeçalho {@code asaas-access-token}; sem token configurado, recusa tudo.
 */
@RestController
@RequestMapping("/webhooks/asaas")
public class AsaasWebhookController {

	private static final int MAX_EVENT_ID = 120;
	private static final int MAX_EVENT_NAME = 60;
	private static final int MAX_ASAAS_ID = 40;

	private final PaymentWebhookService webhooks;
	private final JsonMapper json;
	private final byte[] expectedToken;

	public AsaasWebhookController(PaymentWebhookService webhooks, JsonMapper json, AppProperties props) {
		this.webhooks = webhooks;
		this.json = json;
		String token = props.asaas().webhookToken();
		this.expectedToken = token == null || token.isBlank() ? null : token.getBytes(StandardCharsets.UTF_8);
	}

	@PostMapping
	public ResponseEntity<Void> receive(
			@RequestHeader(name = "asaas-access-token", required = false) String token,
			@RequestBody String body) {
		requireValidToken(token);
		AsaasWebhook event = parse(body);
		webhooks.handle(event, body);
		return ResponseEntity.ok().build();
	}

	private void requireValidToken(String token) {
		boolean valid = expectedToken != null && token != null
				&& MessageDigest.isEqual(expectedToken, token.getBytes(StandardCharsets.UTF_8));
		if (!valid) {
			throw ApiException.unauthorized("INVALID_WEBHOOK_TOKEN", "Token do webhook inválido");
		}
	}

	private AsaasWebhook parse(String body) {
		AsaasWebhook event;
		try {
			event = json.readValue(body, AsaasWebhook.class);
		} catch (JacksonException e) {
			throw invalidEvent();
		}
		if (event == null || !fits(event.id(), MAX_EVENT_ID, true) || !fits(event.event(), MAX_EVENT_NAME, true)
				|| !fits(event.paymentId(), MAX_ASAAS_ID, false) || !fits(event.subscriptionId(), MAX_ASAAS_ID, false)) {
			throw invalidEvent();
		}
		return event;
	}

	private static boolean fits(String value, int max, boolean required) {
		if (value == null || value.isBlank()) {
			return !required;
		}
		return value.length() <= max;
	}

	private static ApiException invalidEvent() {
		return ApiException.badRequest("INVALID_WEBHOOK_EVENT", "Evento do webhook inválido");
	}
}
