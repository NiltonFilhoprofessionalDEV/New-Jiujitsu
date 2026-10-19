package br.com.academia.api.auth;

import java.util.List;
import java.util.Map;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.config.AppProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** POST https://api.resend.com/emails. A chave não entra em log nem em toString. */
@Component
public class ResendPasswordResetMail implements PasswordResetMail {

	private static final String SUBJECT = "Redefinir sua senha do BJJ Pulse";

	private final RestClient http = RestClient.create();
	private final AppProperties.Mail mail;

	public ResendPasswordResetMail(AppProperties props) {
		this.mail = props.mail();
	}

	@Override
	public boolean enabled() {
		return mail.enabled();
	}

	@Override
	public void sendResetLink(String email, String link) {
		String text = """
				Recebemos um pedido para redefinir a senha da sua conta no BJJ Pulse.

				Abra este link (vale por 1 hora e só pode ser usado uma vez):
				%s

				Se você não pediu isso, ignore este e-mail. Sua senha continua a mesma.
				""".formatted(link);
		try {
			http.post()
					.uri("https://api.resend.com/emails")
					.header("Authorization", "Bearer " + mail.apiKey())
					.contentType(MediaType.APPLICATION_JSON)
					.body(Map.of("from", mail.from(), "to", List.of(email), "subject", SUBJECT, "text", text))
					.retrieve()
					.toBodilessEntity();
		} catch (RestClientException ex) {
			throw ApiException.unavailable("RESET_UNAVAILABLE", "Não foi possível enviar o e-mail agora");
		}
	}
}
