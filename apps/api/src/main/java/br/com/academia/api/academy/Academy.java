package br.com.academia.api.academy;

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
		this.name = profile.name();
		this.cnpj = profile.cnpj();
		this.email = profile.email();
		this.phone = profile.phone();
		this.address = profile.address();
		this.city = profile.city();
		this.state = profile.state();
	}

	public AcademyProfile toProfile() {
		return new AcademyProfile(name, cnpj, email, phone, address, city, state);
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
}
