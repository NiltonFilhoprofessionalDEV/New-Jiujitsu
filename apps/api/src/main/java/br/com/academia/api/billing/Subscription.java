package br.com.academia.api.billing;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/** Mensalidade de um aluno: plano, dia e desconto valem para as faturas que ainda vão ser emitidas. */
@Entity
@Table(name = "subscriptions")
public class Subscription {

	/** Condições já validadas pelo service. */
	record Terms(UUID planId, int dueDay, DiscountType discountType, Integer discountValue, String discountReason) {
	}

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "academy_id", nullable = false, updatable = false)
	private UUID academyId;

	@Column(name = "student_id", nullable = false, updatable = false)
	private UUID studentId;

	@Column(name = "plan_id", nullable = false)
	private UUID planId;

	@Column(name = "due_day", nullable = false)
	private short dueDay;

	@Enumerated(EnumType.STRING)
	@Column(name = "discount_type")
	private DiscountType discountType;

	@Column(name = "discount_value")
	private Integer discountValue;

	@Column(name = "discount_reason")
	private String discountReason;

	@Column(name = "next_due_date", nullable = false)
	private LocalDate nextDueDate;

	@Column(name = "started_on", nullable = false, updatable = false)
	private LocalDate startedOn;

	@Column(name = "canceled_at")
	private Instant canceledAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt = Instant.now();

	protected Subscription() {
	}

	Subscription(UUID academyId, UUID studentId, Terms terms, LocalDate startedOn, LocalDate firstDue) {
		this.academyId = academyId;
		this.studentId = studentId;
		this.startedOn = startedOn;
		this.nextDueDate = firstDue;
		apply(terms);
	}

	@PreUpdate
	void touch() {
		updatedAt = Instant.now();
	}

	void apply(Terms t) {
		this.planId = t.planId();
		this.dueDay = (short) t.dueDay();
		this.discountType = t.discountType();
		this.discountValue = t.discountValue();
		this.discountReason = t.discountReason();
	}

	void moveNextDue(LocalDate date) {
		this.nextDueDate = date;
	}

	void advance(int intervalMonths) {
		this.nextDueDate = DueDates.advance(nextDueDate, intervalMonths, dueDay);
	}

	void cancel() {
		this.canceledAt = Instant.now();
	}

	boolean isCanceled() {
		return canceledAt != null;
	}

	int amountCents(Plan plan) {
		return Pricing.amountCents(plan.getPriceCents(), discountType, discountValue);
	}

	public UUID getId() {
		return id;
	}

	public UUID getAcademyId() {
		return academyId;
	}

	public UUID getStudentId() {
		return studentId;
	}

	public UUID getPlanId() {
		return planId;
	}

	public int getDueDay() {
		return dueDay;
	}

	public DiscountType getDiscountType() {
		return discountType;
	}

	public Integer getDiscountValue() {
		return discountValue;
	}

	public String getDiscountReason() {
		return discountReason;
	}

	public LocalDate getNextDueDate() {
		return nextDueDate;
	}

	public LocalDate getStartedOn() {
		return startedOn;
	}
}
