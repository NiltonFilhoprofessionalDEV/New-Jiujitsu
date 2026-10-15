package br.com.academia.api.access;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;

import br.com.academia.api.academy.Academy;
import br.com.academia.api.academy.AcademyRepository;
import br.com.academia.api.access.AcademyAccessDtos.CheckoutRequest;
import br.com.academia.api.access.AcademyAccessDtos.CheckoutResponse;
import br.com.academia.api.access.asaas.AsaasClient;
import br.com.academia.api.access.asaas.AsaasDtos.NewCustomer;
import br.com.academia.api.access.asaas.AsaasDtos.NewSubscription;
import br.com.academia.api.access.asaas.AsaasDtos.Payment;
import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.common.TaxId;
import br.com.academia.api.config.AppProperties;
import br.com.academia.api.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Assinatura recorrente no Asaas: cria cliente e assinatura na primeira vez e devolve a página da fatura
 * em aberto, onde o gestor paga com Pix, boleto ou cartão (o cartão nunca passa por aqui).
 */
@Service
public class CheckoutService {

	static final String BILLING_TYPE_ANY = "UNDEFINED";
	static final String CYCLE = "MONTHLY";
	static final String DESCRIPTION = "BJJ Pulse - assinatura mensal";

	private final AcademyRepository academies;
	private final UserRepository users;
	private final AcademyAccessService access;
	private final AsaasClient asaas;
	private final AppProperties props;
	private final Clock clock;

	public CheckoutService(AcademyRepository academies, UserRepository users, AcademyAccessService access,
			AsaasClient asaas, AppProperties props, Clock clock) {
		this.academies = academies;
		this.users = users;
		this.access = access;
		this.asaas = asaas;
		this.props = props;
		this.clock = clock;
	}

	/** Sem rollback em erro esperado: o cliente criado no Asaas fica gravado mesmo se a assinatura falhar. */
	@Transactional(noRollbackFor = ApiException.class)
	public CheckoutResponse checkout(CurrentUser me, CheckoutRequest req) {
		if (!access.enforced() || !asaas.enabled()) {
			throw AsaasClient.unavailable();
		}
		Academy academy = academies.findForUpdate(me.academyId())
				.orElseThrow(() -> ApiException.notFound("ACADEMY_NOT_FOUND", "Academia não encontrada"));
		if (academy.getAsaasSubscriptionId() == null) {
			subscribe(academy, me, req == null ? null : req.cpfCnpj());
		}
		return new CheckoutResponse(openInvoiceUrl(academy.getAsaasSubscriptionId()));
	}

	private void subscribe(Academy academy, CurrentUser me, String rawTaxId) {
		if (academy.getAsaasCustomerId() == null) {
			String taxId = TaxId.normalize(rawTaxId);
			if (taxId == null) {
				throw ApiException.badRequest("TAX_ID_REQUIRED", "Informe o CPF ou CNPJ de quem vai pagar");
			}
			String email = users.findById(me.userId()).orElseThrow().getEmail();
			academy.linkAsaasCustomer(asaas.createCustomer(
					new NewCustomer(academy.getName(), taxId, email, academy.getId().toString())));
		}
		academy.linkAsaasSubscription(asaas.createSubscription(new NewSubscription(
				academy.getAsaasCustomerId(), BILLING_TYPE_ANY, props.subscription().monthlyPrice(),
				firstDueDate(academy).toString(), CYCLE, DESCRIPTION, academy.getId().toString())));
	}

	/** O primeiro vencimento cai no fim do acesso atual: quem assina no teste não perde dias grátis. */
	private LocalDate firstDueDate(Academy academy) {
		LocalDate today = LocalDate.now(clock);
		LocalDate accessEnd = LocalDate.ofInstant(access.evaluate(academy).accessUntil(), clock.getZone());
		return accessEnd.isAfter(today) ? accessEnd : today;
	}

	private String openInvoiceUrl(String subscriptionId) {
		return asaas.payments(subscriptionId).stream()
				.filter(Payment::isOpen)
				.filter(p -> p.invoiceUrl() != null)
				.min(Comparator.comparing(Payment::dueDate, Comparator.nullsLast(Comparator.naturalOrder())))
				.map(Payment::invoiceUrl)
				.orElseThrow(() -> ApiException.conflict("NOTHING_TO_PAY",
						"Nenhuma fatura em aberto agora. Se acabou de assinar, tente de novo em instantes."));
	}
}
