package br.com.academia.api.access;

import static br.com.academia.api.ApiTestSupport.CTX;
import static br.com.academia.api.ApiTestSupport.auth;
import static br.com.academia.api.ApiTestSupport.authJson;
import static br.com.academia.api.ApiTestSupport.registerOwner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import br.com.academia.api.StaffFixture;
import br.com.academia.api.TestcontainersConfiguration;
import br.com.academia.api.access.asaas.AsaasClient;
import br.com.academia.api.access.asaas.AsaasDtos.NewSubscription;
import br.com.academia.api.access.asaas.AsaasDtos.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, StaffFixture.class })
class CheckoutIntegrationTest {

	private static final String CHECKOUT = CTX + "/academy-access/checkout";
	private static final String VALID_CPF = "{\"cpfCnpj\":\"529.982.247-25\"}";
	private static final String INVOICE_URL = "https://sandbox.asaas.com/i/fatura-de-teste";
	private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

	@Autowired
	MockMvc mvc;

	@Autowired
	StaffFixture staff;

	@MockitoBean
	AsaasClient asaas;

	@BeforeEach
	void asaasAnswers() {
		when(asaas.enabled()).thenReturn(true);
		when(asaas.createCustomer(any())).thenAnswer(i -> "cus_" + UUID.randomUUID());
		when(asaas.createSubscription(any())).thenAnswer(i -> "sub_" + UUID.randomUUID());
		when(asaas.payments(anyString())).thenReturn(List.of(
				new Payment("pay_pago", "RECEIVED", "2026-01-01", "https://sandbox.asaas.com/i/paga"),
				new Payment("pay_aberto", "PENDING", "2026-02-01", INVOICE_URL)));
	}

	@Test
	void firstCheckoutSubscribesFromTheEndOfTheTrial() throws Exception {
		String owner = registerOwner(mvc);

		mvc.perform(authJson(post(CHECKOUT), owner, VALID_CPF))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.invoiceUrl").value(INVOICE_URL));

		ArgumentCaptor<NewSubscription> sent = ArgumentCaptor.forClass(NewSubscription.class);
		verify(asaas).createSubscription(sent.capture());
		assertThat(sent.getValue().value()).isEqualByComparingTo(new BigDecimal("100.00"));
		assertThat(sent.getValue().billingType()).isEqualTo("UNDEFINED");
		assertThat(sent.getValue().cycle()).isEqualTo("MONTHLY");
		assertThat(sent.getValue().nextDueDate()).isEqualTo(LocalDate.now(ZONE).plusDays(14).toString());
		mvc.perform(auth(get(CTX + "/academy-access"), owner)).andExpect(jsonPath("$.subscribed").value(true));
	}

	@Test
	void laterCheckoutsReuseTheSubscription() throws Exception {
		String owner = registerOwner(mvc);
		mvc.perform(authJson(post(CHECKOUT), owner, VALID_CPF)).andExpect(status().isOk());

		mvc.perform(auth(post(CHECKOUT), owner))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.invoiceUrl").value(INVOICE_URL));

		verify(asaas, times(1)).createCustomer(any());
		verify(asaas, times(1)).createSubscription(any());
	}

	@Test
	void firstCheckoutNeedsAValidTaxId() throws Exception {
		String owner = registerOwner(mvc);

		mvc.perform(auth(post(CHECKOUT), owner))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("TAX_ID_REQUIRED"));
		mvc.perform(authJson(post(CHECKOUT), owner, "{\"cpfCnpj\":\"111.111.111-11\"}"))
				.andExpect(jsonPath("$.code").value("INVALID_TAX_ID"));
		verify(asaas, never()).createCustomer(any());
	}

	@Test
	void nothingOpenToPay() throws Exception {
		when(asaas.payments(anyString())).thenReturn(List.of(
				new Payment("pay_pago", "CONFIRMED", "2026-01-01", "https://sandbox.asaas.com/i/paga")));
		String owner = registerOwner(mvc);

		mvc.perform(authJson(post(CHECKOUT), owner, VALID_CPF))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("NOTHING_TO_PAY"));
	}

	@Test
	void disabledPaymentAnswersUnavailable() throws Exception {
		when(asaas.enabled()).thenReturn(false);

		mvc.perform(authJson(post(CHECKOUT), registerOwner(mvc), VALID_CPF))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.code").value("PAYMENT_UNAVAILABLE"));
	}

	@Test
	void instructorsCannotSubscribe() throws Exception {
		String instructor = staff.instructorTokenFor(mvc, registerOwner(mvc));

		mvc.perform(authJson(post(CHECKOUT), instructor, VALID_CPF)).andExpect(status().isForbidden());
	}
}
