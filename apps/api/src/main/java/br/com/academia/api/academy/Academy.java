package br.com.academia.api.academy;

import static br.com.academia.api.common.TextNormalizer.blankToNull;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "academies")
public class Academy {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private String slug;

	private String cnpj;
	private String email;
	private String phone;
	private String address;
	private String city;
	private String state;

	@Column(name = "logo_path")
	private String logoPath;

	@Column(name = "banner_path")
	private String bannerPath;

	@Column(name = "trial_ends_at", nullable = false)
	private Instant trialEndsAt;

	@Column(name = "paid_until")
	private Instant paidUntil;

	@Column(name = "asaas_customer_id")
	private String asaasCustomerId;

	@Column(name = "asaas_subscription_id", unique = true)
	private String asaasSubscriptionId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt = Instant.now();

	protected Academy() {
	}

	public Academy(String name, String slug, Instant trialEndsAt) {
		this.name = name;
		this.slug = slug;
		this.trialEndsAt = trialEndsAt;
	}

	@PreUpdate
	void touch() {
		updatedAt = Instant.now();
	}

	public void updateProfile(AcademyProfile profile) {
		this.name = profile.name().trim();
		this.cnpj = blankToNull(profile.cnpj());
		this.email = blankToNull(profile.email());
		this.phone = blankToNull(profile.phone());
		this.address = blankToNull(profile.address());
		this.city = blankToNull(profile.city());
		this.state = blankToNull(profile.state());
	}

	public AcademyProfile toProfile() {
		return new AcademyProfile(name, cnpj, email, phone, address, city, state);
	}

	/** Estende o teste a partir do maior entre o fim atual e {@code now}: teste vencido recomeça de hoje. */
	public void extendTrial(Duration extra, Instant now) {
		trialEndsAt = latest(trialEndsAt, now).plus(extra);
	}

	/** Estende o período pago a partir do maior entre o fim atual e {@code now}. */
	public void extendPaidPeriod(Duration extra, Instant now) {
		paidUntil = latest(paidUntil, now).plus(extra);
	}

	/** Garante o período pago até {@code until}; nunca encurta (eventos repetidos dão o mesmo resultado). */
	public void payUntil(Instant until) {
		paidUntil = latest(paidUntil, until);
	}

	public void linkAsaasCustomer(String customerId) {
		this.asaasCustomerId = customerId;
	}

	public void linkAsaasSubscription(String subscriptionId) {
		this.asaasSubscriptionId = subscriptionId;
	}

	private static Instant latest(Instant current, Instant other) {
		return current != null && current.isAfter(other) ? current : other;
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getSlug() {
		return slug;
	}

	public String getLogoPath() {
		return logoPath;
	}

	public Instant getTrialEndsAt() {
		return trialEndsAt;
	}

	public Instant getPaidUntil() {
		return paidUntil;
	}

	public String getAsaasCustomerId() {
		return asaasCustomerId;
	}

	public String getAsaasSubscriptionId() {
		return asaasSubscriptionId;
	}
}
