package br.com.academia.api.billing;

import java.time.Instant;
import java.util.UUID;

import br.com.academia.api.billing.PlanDtos.PlanRequest;
import br.com.academia.api.common.TextNormalizer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/** Plano de mensalidade. Padrão e padrão kids são vinculados automaticamente a novos alunos. */
@Entity
@Table(name = "plans")
public class Plan {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "academy_id", nullable = false, updatable = false)
	private UUID academyId;

	@Column(nullable = false)
	private String name;

	private String description;

	@Column(name = "price_cents", nullable = false)
	private int priceCents;

	@Column(name = "interval_months", nullable = false)
	private short intervalMonths;

	@Column(name = "due_day", nullable = false)
	private short dueDay;

	@Column(nullable = false)
	private boolean active = true;

	@Column(name = "is_default", nullable = false)
	private boolean isDefault;

	@Column(name = "is_kids_default", nullable = false)
	private boolean isKidsDefault;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt = Instant.now();

	protected Plan() {
	}

	Plan(UUID academyId) {
		this.academyId = academyId;
	}

	@PreUpdate
	void touch() {
		updatedAt = Instant.now();
	}

	/** Os marcadores de padrão são trocados à parte pelo service, que antes desmarca o plano anterior. */
	void apply(PlanRequest r) {
		this.name = r.name().trim();
		this.description = TextNormalizer.blankToNull(r.description());
		this.priceCents = r.priceCents();
		this.intervalMonths = r.intervalMonths().shortValue();
		this.dueDay = r.dueDay().shortValue();
		this.active = r.active();
	}

	void setDefault(boolean value) {
		this.isDefault = value;
	}

	void setKidsDefault(boolean value) {
		this.isKidsDefault = value;
	}

	public UUID getId() {
		return id;
	}

	public UUID getAcademyId() {
		return academyId;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public int getPriceCents() {
		return priceCents;
	}

	public int getIntervalMonths() {
		return intervalMonths;
	}

	public int getDueDay() {
		return dueDay;
	}

	public boolean isActive() {
		return active;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public boolean isKidsDefault() {
		return isKidsDefault;
	}
}
