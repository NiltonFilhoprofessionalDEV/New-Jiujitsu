package br.com.academia.api.billing;

import static br.com.academia.api.ApiTestSupport.CTX;
import static br.com.academia.api.ApiTestSupport.EMERGENCY_FIELDS;
import static br.com.academia.api.ApiTestSupport.auth;
import static br.com.academia.api.ApiTestSupport.authJson;
import static br.com.academia.api.ApiTestSupport.read;
import static br.com.academia.api.ApiTestSupport.registerOwner;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

import br.com.academia.api.StaffFixture;
import br.com.academia.api.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, StaffFixture.class })
class BillingFlowIntegrationTest {

	private static final int PRICE = 15_000;

	@Autowired
	MockMvc mvc;

	@Autowired
	StaffFixture staff;

	@Autowired
	JdbcClient jdbc;

	@Autowired
	Clock clock;

	String owner;
	LocalDate today;
	/** Vencimento hoje: exatamente uma fatura emitida, e a seguinte fica fora da janela de 10 dias. */
	int dueToday;

	@BeforeEach
	void setUp() throws Exception {
		owner = registerOwner(mvc);
		today = LocalDate.now(clock);
		dueToday = Math.min(today.getDayOfMonth(), 28);
	}

	@Test
	void newStudentsJoinTheDefaultPlanForTheirAge() throws Exception {
		String adult = createPlan("Adulto", true, false);
		createPlan("Outro adulto", true, false);
		String kids = createPlan("Kids", false, true);
		mvc.perform(auth(get(CTX + "/plans"), owner))
				.andExpect(jsonPath("$[?(@.name == 'Adulto')].isDefault").value(false))
				.andExpect(jsonPath("$[?(@.name == 'Outro adulto')].isDefault").value(true));
		mvc.perform(authJson(post(CTX + "/plans"), owner, planBody("Inativo", false, true, false)))
				.andExpect(jsonPath("$.code").value("INVALID_PLAN_DEFAULT"));

		createStudent("TRAINING", "Criança Teste", today.minusYears(10));
		createStudent("RESPONSIBLE", "Responsável Teste", null);
		mvc.perform(auth(get(CTX + "/subscriptions"), owner))
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].plan.id").value(kids));
		mvc.perform(auth(get(CTX + "/plans"), owner))
				.andExpect(jsonPath("$[?(@.id == '" + adult + "')].subscribers").value(0));
	}

	@Test
	void issuesSettlesReversesAndCancelsInvoices() throws Exception {
		String plan = createPlan("Mensal", false, false);
		String student = createStudent("TRAINING", "Ana Lima", null);
		link(student, plan, "\"discountType\":\"PERCENT\",\"discountValue\":10")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.amountCents").value(13_500))
				.andExpect(jsonPath("$.nextDue").value(today.toString()));
		link(student, plan, "").andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("STUDENT_ALREADY_SUBSCRIBED"));

		String invoice = read(listInvoices("")
				.andExpect(jsonPath("$.page.content", hasSize(1)))
				.andExpect(jsonPath("$.page.content[0].situation").value("OPEN"))
				.andExpect(jsonPath("$.totals.open.cents").value(13_500))
				.andReturn(), "$.page.content[0].id");

		pay(invoice, today.plusDays(1)).andExpect(jsonPath("$.code").value("PAYMENT_IN_FUTURE"));
		pay(invoice, today).andExpect(status().isOk()).andExpect(jsonPath("$.situation").value("PAID"))
				.andExpect(jsonPath("$.paymentMethod").value("PIX"));
		pay(invoice, today).andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INVOICE_STATUS_CHANGED"));
		listInvoices("?situation=paid").andExpect(jsonPath("$.totals.paid.count").value(1));

		action(invoice, "reverse").andExpect(jsonPath("$.situation").value("OPEN"))
				.andExpect(jsonPath("$.paidOn").doesNotExist());
		action(invoice, "cancel").andExpect(jsonPath("$.situation").value("CANCELED"));
		listInvoices("?situation=open").andExpect(jsonPath("$.page.content", hasSize(0)));
	}

	@Test
	void overdueInvoicesSurviveTermChangesAndCancellation() throws Exception {
		String plan = createPlan("Mensal", false, false);
		String student = createStudent("TRAINING", "Bia Costa", null);
		String subscription = read(link(student, plan, "").andReturn(), "$.id");
		jdbc.sql("update invoices set due_date = due_date - 3 where student_id = :id")
				.param("id", UUID.fromString(student)).update();

		listInvoices("?situation=overdue")
				.andExpect(jsonPath("$.page.content[0].situation").value("OVERDUE"))
				.andExpect(jsonPath("$.overdue.students").value(1))
				.andExpect(jsonPath("$.overdue.cents").value(PRICE));
		mvc.perform(authJson(put(CTX + "/subscriptions/" + subscription), owner, """
				{"planId":"%s","discountType":"FIXED","discountValue":5000}
				""".formatted(plan)))
				.andExpect(jsonPath("$.amountCents").value(10_000))
				.andExpect(jsonPath("$.overdueCents").value(PRICE))
				.andExpect(jsonPath("$.overdueSince").value(today.minusDays(3).toString()));

		mvc.perform(auth(delete(CTX + "/subscriptions/" + subscription), owner)).andExpect(status().isNoContent());
		mvc.perform(auth(get(CTX + "/subscriptions"), owner)).andExpect(jsonPath("$", hasSize(0)));
		listInvoices("?situation=overdue&q=bia").andExpect(jsonPath("$.page.totalElements").value(1));
		listInvoices("?month=2026-13").andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_FILTER"));
	}

	@Test
	void inactiveStudentsAreNotBilledAndBilledStudentsCannotBeDeleted() throws Exception {
		String plan = createPlan("Mensal", false, false);
		String paused = createStudent("TRAINING", "Caio Reis", null);
		mvc.perform(auth(post(CTX + "/students/" + paused + "/deactivate"), owner)).andExpect(status().isOk());
		link(paused, plan, "").andExpect(status().isCreated());
		listInvoices("").andExpect(jsonPath("$.page.totalElements").value(0));
		mvc.perform(auth(delete(CTX + "/students/" + paused), owner)).andExpect(status().isNoContent());

		String billed = createStudent("TRAINING", "Duda Melo", null);
		link(billed, plan, "").andExpect(status().isCreated());
		mvc.perform(auth(delete(CTX + "/students/" + billed), owner))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("STUDENT_HAS_INVOICES"));
	}

	@Test
	void onlyManagersSeeMoney() throws Exception {
		String instructor = staff.instructorTokenFor(mvc, owner);
		mvc.perform(auth(get(CTX + "/plans"), instructor)).andExpect(status().isForbidden());
		mvc.perform(auth(get(CTX + "/subscriptions"), instructor)).andExpect(status().isForbidden());
		mvc.perform(auth(get(CTX + "/invoices"), instructor)).andExpect(status().isForbidden());
	}

	private String createPlan(String name, boolean isDefault, boolean isKidsDefault) throws Exception {
		return read(mvc.perform(authJson(post(CTX + "/plans"), owner, planBody(name, true, isDefault, isKidsDefault)))
				.andExpect(status().isCreated())
				.andReturn(), "$.id");
	}

	private String planBody(String name, boolean active, boolean isDefault, boolean isKidsDefault) {
		return """
				{"name":"%s","priceCents":%d,"intervalMonths":1,"dueDay":%d,
				 "active":%b,"isDefault":%b,"isKidsDefault":%b}
				""".formatted(name, PRICE, dueToday, active, isDefault, isKidsDefault);
	}

	private String createStudent(String kind, String name, LocalDate birthDate) throws Exception {
		String birth = birthDate == null ? "null" : "\"" + birthDate + "\"";
		return read(mvc.perform(authJson(post(CTX + "/students"), owner, """
				{"kind":"%s","name":"%s","birthDate":%s,%s}
				""".formatted(kind, name, birth, EMERGENCY_FIELDS)))
				.andExpect(status().isCreated())
				.andReturn(), "$.id");
	}

	/** {@code extraTerms}: campos de desconto já em JSON, ou vazio. */
	private ResultActions link(String studentId, String planId, String extraTerms) throws Exception {
		String terms = extraTerms.isEmpty() ? "" : "," + extraTerms;
		return mvc.perform(authJson(post(CTX + "/subscriptions"), owner, """
				{"studentId":"%s","firstDueDate":"%s","terms":{"planId":"%s","dueDay":%d%s}}
				""".formatted(studentId, today, planId, dueToday, terms)));
	}

	private ResultActions listInvoices(String query) throws Exception {
		return mvc.perform(auth(get(CTX + "/invoices" + query), owner));
	}

	private ResultActions pay(String invoiceId, LocalDate paidOn) throws Exception {
		return mvc.perform(authJson(post(CTX + "/invoices/" + invoiceId + "/pay"), owner, """
				{"paidOn":"%s","method":"PIX"}
				""".formatted(paidOn)));
	}

	private ResultActions action(String invoiceId, String name) throws Exception {
		return mvc.perform(auth(post(CTX + "/invoices/" + invoiceId + "/" + name), owner));
	}
}
