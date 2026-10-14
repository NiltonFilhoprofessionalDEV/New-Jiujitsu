package br.com.academia.api.billing;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import br.com.academia.api.common.ApiException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/** Fatura. "Vencida" não é um status gravado: é {@code OPEN} com vencimento antes de hoje. */
@Entity
@Table(name = "invoices")
public class Invoice {

	public enum Status {
		OPEN, PAID, CANCELED
	}

	public enum PaymentMethod {
		CASH, PIX, CARD, TRANSFER, OTHER
	}

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "academy_id", nullable = false, updatable = false)
	private UUID academyId;

	@Column(name = "student_id", nullable = false, updatable = false)
	private UUID studentId;

	@Column(name = "subscription_id", updatable = false)
	private UUID subscriptionId;

	@Column(nullable = false, updatable = false)
	private String description;

	@Column(name = "amount_cents", nullable = false, updatable = false)
	private int amountCents;

	@Column(name = "due_date", nullable = false, updatable = false)
	private LocalDate dueDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status = Status.OPEN;

	@Column(name = "paid_on")
	private LocalDate paidOn;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method")
	private PaymentMethod paymentMethod;

	@Column(name = "paid_recorded_by")
	private UUID paidRecordedBy;

	@Column(name = "canceled_at")
	private Instant canceledAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt = Instant.now();

	protected Invoice() {
	}

	private Invoice(Subscription s, String description, int amountCents, LocalDate dueDate) {
		this.academyId = s.getAcademyId();
		this.studentId = s.getStudentId();
		this.subscriptionId = s.getId();
		this.description = description;
		this.amountCents = amountCents;
		this.dueDate = dueDate;
	}

	static Invoice forCycle(Subscription s, String description, int amountCents) {
		return new Invoice(s, description, amountCents, s.getNextDueDate());
	}

	@PreUpdate
	void touch() {
		updatedAt = Instant.now();
	}

	void markPaid(LocalDate on, PaymentMethod method, UUID recordedBy) {
		requireStatus(Status.OPEN);
		this.status = Status.PAID;
		this.paidOn = on;
		this.paymentMethod = method;
		this.paidRecordedBy = recordedBy;
	}

	void cancel() {
		requireStatus(Status.OPEN);
		this.status = Status.CANCELED;
		this.canceledAt = Instant.now();
	}

	/** Estorno: o recebimento foi registrado por engano e a fatura volta a ser devida. */
	void reversePayment() {
		requireStatus(Status.PAID);
		this.status = Status.OPEN;
		this.paidOn = null;
		this.paymentMethod = null;
		this.paidRecordedBy = null;
	}

	private void requireStatus(Status expected) {
		if (status != expected) {
			throw ApiException.conflict("INVOICE_STATUS_CHANGED",
					"A fatura não está mais " + (expected == Status.OPEN ? "em aberto" : "paga"));
		}
	}

	public UUID getId() {
		return id;
	}

	public UUID getStudentId() {
		return studentId;
	}

	public String getDescription() {
		return description;
	}

	public int getAmountCents() {
		return amountCents;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public Status getStatus() {
		return status;
	}

	public LocalDate getPaidOn() {
		return paidOn;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}
}
