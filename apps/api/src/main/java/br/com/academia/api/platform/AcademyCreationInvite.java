package br.com.academia.api.platform;

import java.time.Instant;
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

/** Convite do admin da plataforma para criar uma academia. "Expirado" deriva de {@code expiresAt}. */
@Entity
@Table(name = "academy_creation_invites")
public class AcademyCreationInvite {

	public enum Status {
		PENDING, ACCEPTED, REVOKED
	}

	public enum View {
		PENDING, ACCEPTED, EXPIRED, REVOKED
	}

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, unique = true, updatable = false)
	private String token;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status = Status.PENDING;

	@Column(name = "expires_at", nullable = false, updatable = false)
	private Instant expiresAt;

	@Column(name = "created_by", updatable = false)
	private UUID createdBy;

	@Column(name = "accepted_academy_id")
	private UUID acceptedAcademyId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt = Instant.now();

	protected AcademyCreationInvite() {
	}

	AcademyCreationInvite(String token, UUID createdBy, Instant expiresAt) {
		this.token = token;
		this.createdBy = createdBy;
		this.expiresAt = expiresAt;
	}

	@PreUpdate
	void touch() {
		updatedAt = Instant.now();
	}

	View view(Instant now) {
		return switch (status) {
			case ACCEPTED -> View.ACCEPTED;
			case REVOKED -> View.REVOKED;
			case PENDING -> expiresAt.isAfter(now) ? View.PENDING : View.EXPIRED;
		};
	}

	void revoke() {
		this.status = Status.REVOKED;
	}

	void accept(UUID academyId) {
		this.status = Status.ACCEPTED;
		this.acceptedAcademyId = academyId;
	}

	public UUID getId() {
		return id;
	}

	public String getToken() {
		return token;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
