package br.com.academia.api.access.asaas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import br.com.academia.api.access.asaas.AsaasDtos.NewSubscription;
import br.com.academia.api.access.asaas.AsaasDtos.Payment;
import br.com.academia.api.common.ApiException;
import br.com.academia.api.config.AppProperties;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Contrato HTTP com o Asaas contra um servidor local: cabeçalhos, JSON e tradução de erros. */
class AsaasClientTest {

	private static final String API_KEY = "chave-local-de-teste";

	private HttpServer server;
	private final Map<String, String> received = new ConcurrentHashMap<>();
	private AsaasClient client;

	@BeforeEach
	void start() throws IOException {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/v3/subscriptions", this::subscriptions);
		server.createContext("/v3/customers", ex -> respond(ex, 400,
				"{\"errors\":[{\"code\":\"invalid_cpfCnpj\",\"description\":\"O CPF/CNPJ informado é inválido.\"}]}"));
		server.start();
		client = clientFor("http://127.0.0.1:" + server.getAddress().getPort() + "/v3", API_KEY);
	}

	@AfterEach
	void stop() {
		server.stop(0);
	}

	@Test
	void createsSubscriptionWithAuthHeadersAndJson() {
		String id = client.createSubscription(new NewSubscription("cus_1", "UNDEFINED", new BigDecimal("100.00"),
				"2026-11-01", "MONTHLY", "Assinatura", "academia-1"));

		assertThat(id).isEqualTo("sub_123");
		assertThat(received.get("access_token")).isEqualTo(API_KEY);
		assertThat(received.get("user-agent")).isEqualTo("BJJPulse/1.0");
		assertThat(received.get("body")).contains("\"nextDueDate\":\"2026-11-01\"", "\"value\":100.00",
				"\"billingType\":\"UNDEFINED\"");
	}

	@Test
	void readsPaymentsIgnoringUnknownFields() {
		List<Payment> payments = client.payments("sub_123");

		assertThat(payments).singleElement().satisfies(p -> {
			assertThat(p.isOpen()).isTrue();
			assertThat(p.invoiceUrl()).isEqualTo("https://sandbox.asaas.com/i/abc");
		});
	}

	@Test
	void providerErrorsBecomeBadGateway() {
		assertThatThrownBy(() -> client.createCustomer(new AsaasDtos.NewCustomer("A", "1", "a@b.c", "x")))
				.isInstanceOf(ApiException.class)
				.extracting(e -> ((ApiException) e).code())
				.isEqualTo("PAYMENT_PROVIDER_ERROR");
	}

	@Test
	void withoutKeyThePaymentIsUnavailable() {
		AsaasClient disabled = clientFor("http://127.0.0.1:1/v3", "");

		assertThat(disabled.enabled()).isFalse();
		assertThatThrownBy(() -> disabled.payments("sub_1"))
				.extracting(e -> ((ApiException) e).code())
				.isEqualTo("PAYMENT_UNAVAILABLE");
	}

	private void subscriptions(HttpExchange ex) throws IOException {
		received.put("access_token", String.valueOf(ex.getRequestHeaders().getFirst("access_token")));
		received.put("user-agent", String.valueOf(ex.getRequestHeaders().getFirst("User-Agent")));
		if ("POST".equals(ex.getRequestMethod())) {
			received.put("body", new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
			respond(ex, 200, "{\"object\":\"subscription\",\"id\":\"sub_123\",\"status\":\"ACTIVE\"}");
			return;
		}
		respond(ex, 200, """
				{"object":"list","hasMore":false,"totalCount":1,"data":[
				  {"object":"payment","id":"pay_1","status":"PENDING","dueDate":"2026-11-01","value":100,
				   "invoiceUrl":"https://sandbox.asaas.com/i/abc","billingType":"UNDEFINED"}]}
				""");
	}

	private static void respond(HttpExchange ex, int status, String json) throws IOException {
		byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
		ex.getResponseHeaders().add("Content-Type", "application/json");
		ex.sendResponseHeaders(status, bytes.length);
		ex.getResponseBody().write(bytes);
		ex.close();
	}

	private static AsaasClient clientFor(String baseUrl, String apiKey) {
		AppProperties props = new AppProperties(null, null, null, null, null, null, null, null, null,
				new AppProperties.Asaas(baseUrl, apiKey, null), null, new AppProperties.Registration(false));
		return new AsaasClient(props);
	}
}
