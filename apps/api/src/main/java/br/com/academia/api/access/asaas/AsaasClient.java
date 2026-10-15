package br.com.academia.api.access.asaas;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.academia.api.access.asaas.AsaasDtos.Created;
import br.com.academia.api.access.asaas.AsaasDtos.NewCustomer;
import br.com.academia.api.access.asaas.AsaasDtos.NewSubscription;
import br.com.academia.api.access.asaas.AsaasDtos.Payment;
import br.com.academia.api.access.asaas.AsaasDtos.PaymentPage;
import br.com.academia.api.common.ApiException;
import br.com.academia.api.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/** Cliente da API v3 do Asaas. Falhas viram 502 sem expor a resposta do Asaas a quem chamou. */
@Component
public class AsaasClient {

	private static final Logger log = LoggerFactory.getLogger(AsaasClient.class);
	private static final String USER_AGENT = "BJJPulse/1.0";
	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
	private static final Duration READ_TIMEOUT = Duration.ofSeconds(60);
	/** Os códigos de erro do Asaas ajudam a diagnosticar sem levar dados pessoais para o log. */
	private static final Pattern ERROR_CODE = Pattern.compile("\"code\"\\s*:\\s*\"([\\w.-]{1,60})\"");

	private final RestClient http;
	private final boolean enabled;

	public AsaasClient(AppProperties props) {
		AppProperties.Asaas config = props.asaas();
		this.enabled = config.enabled();
		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
				HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build());
		factory.setReadTimeout(READ_TIMEOUT);
		this.http = RestClient.builder()
				.baseUrl(config.baseUrl())
				.requestFactory(factory)
				.defaultHeader("access_token", enabled ? config.apiKey() : "")
				.defaultHeader("User-Agent", USER_AGENT)
				.build();
	}

	public boolean enabled() {
		return enabled;
	}

	public String createCustomer(NewCustomer customer) {
		return call("criar cliente", () -> post("/customers", customer).id());
	}

	public String createSubscription(NewSubscription subscription) {
		return call("criar assinatura", () -> post("/subscriptions", subscription).id());
	}

	public List<Payment> payments(String subscriptionId) {
		return call("listar cobranças", () -> {
			PaymentPage page = http.get().uri("/subscriptions/{id}/payments", subscriptionId)
					.retrieve().body(PaymentPage.class);
			return page == null || page.data() == null ? List.of() : page.data();
		});
	}

	private Created post(String path, Object body) {
		Created created = http.post().uri(path).contentType(MediaType.APPLICATION_JSON).body(body)
				.retrieve().body(Created.class);
		if (created == null || created.id() == null) {
			throw new RestClientException("Resposta sem id");
		}
		return created;
	}

	private <T> T call(String operation, Supplier<T> request) {
		if (!enabled) {
			throw unavailable();
		}
		try {
			return request.get();
		} catch (RestClientResponseException e) {
			log.warn("Asaas: falha ao {} (HTTP {}, códigos {})", operation, e.getStatusCode().value(),
					errorCodes(e.getResponseBodyAsString()));
			throw providerError();
		} catch (RestClientException e) {
			log.warn("Asaas: falha ao {} ({})", operation, e.getClass().getSimpleName());
			throw providerError();
		}
	}

	private static List<String> errorCodes(String body) {
		Matcher matcher = ERROR_CODE.matcher(body == null ? "" : body);
		return matcher.results().map(m -> m.group(1)).limit(5).toList();
	}

	public static ApiException unavailable() {
		return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "PAYMENT_UNAVAILABLE",
				"Pagamento online indisponível no momento");
	}

	private static ApiException providerError() {
		return new ApiException(HttpStatus.BAD_GATEWAY, "PAYMENT_PROVIDER_ERROR",
				"Não foi possível falar com o serviço de pagamento. Tente novamente em instantes.");
	}
}
