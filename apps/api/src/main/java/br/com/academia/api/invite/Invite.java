package br.com.academia.api.invite;

import java.time.Instant;
import java.util.UUID;

import br.com.academia.api.student.StudentEnums.Kind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "invites")
public class Invite {

	/** Estado persistido. "Expirado" não é gravado: deriva de {@code expiresAt}. */
	public enum Status {
		PENDING, ACCEPTED, REVOKED
	}

	/** Estado exibido, já considerando a validade. */
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
	private Kind kind;

	private String name;
	private String email;
	private String phone;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status = Status.PENDING;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(nullable = false)
	private int opens;

	@Column(name = "accepted_student_id")
	private UUID acceptedStudentId;

	@Column(name = "created_by", updatable = false)
	private UUID createdBy;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt = Instant.now();

	protected Invite() {
	}

	Invite(UUID academyId, String token, InviteDtos.CreateInviteRequest data, UUID createdBy, Instant expiresAt) {
		this.academyId = academyId;
		this.token = token;
		this.kind = data.kind();
		this.name = data.name();
		this.email = data.email();
		this.phone = data.phone();
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

	void registerOpen() {
		opens++;
	}

	void revoke() {
		this.status = Status.REVOKED;
	}

	void renew(Instant newExpiry) {
		this.expiresAt = newExpiry;
	}

	void accept(UUID studentId) {
		this.status = Status.ACCEPTED;
		this.acceptedStudentId = studentId;
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

	public Kind getKind() {
		return kind;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getPhone() {
		return phone;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public int getOpens() {
		return opens;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
