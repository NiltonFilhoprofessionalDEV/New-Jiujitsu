package br.com.academia.api.team;

import java.time.Instant;
import java.util.UUID;

import br.com.academia.api.membership.MembershipRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/** Convite de equipe. "Expirado" não é gravado: deriva de {@code expiresAt}. */
@Entity
@Table(name = "staff_invites")
public class StaffInvite {

	public enum Status {
		PENDING, ACCEPTED, REVOKED
	}

	public enum View {
		PENDING, ACCEPTED, EXPIRED, REVOKED
	}

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "academy_id", nullable = false, updatable = false)
	private UUID academyId;

	@Column(nullable = false, unique = true, updatable = false)
	private String token;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false)
	private MembershipRole role;

	@Column(updatable = false)
	private String name;

	@Column(updatable = false)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status = Status.PENDING;

	@Column(name = "expires_at", nullable = false, updatable = false)
	private Instant expiresAt;

	@Column(name = "accepted_membership_id")
	private UUID acceptedMembershipId;

	@Column(name = "created_by", updatable = false)
	private UUID createdBy;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt = Instant.now();

	protected StaffInvite() {
	}

	StaffInvite(UUID academyId, String token, MembershipRole role, String name, String email, UUID createdBy,
			Instant expiresAt) {
		this.academyId = academyId;
		this.token = token;
		this.role = role;
		this.name = name;
		this.email = email;
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

	void accept(UUID membershipId) {
		this.status = Status.ACCEPTED;
		this.acceptedMembershipId = membershipId;
	}

	public UUID getId() {
		return id;
	}

	public UUID getAcademyId() {
		return academyId;
	}

	public String getToken() {
		return token;
	}

	public MembershipRole getRole() {
		return role;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
