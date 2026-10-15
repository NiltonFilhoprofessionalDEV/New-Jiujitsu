package br.com.academia.api.invite;

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
import jakarta.persistence.Table;

/** Cadastro feito pelo link de matrícula: vira aluno só quando o gestor aprova. */
@Entity
@Table(name = "enrollment_requests")
public class EnrollmentRequest {

	public enum Status {
		PENDING, APPROVED, REJECTED
	}

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "academy_id", nullable = false, updatable = false)
	private UUID academyId;

	@Column(name = "user_id", nullable = false, updatable = false)
	private UUID userId;

	@Column(name = "membership_id", nullable = false, updatable = false)
	private UUID membershipId;

	@Column(nullable = false, updatable = false)
	private String name;

	@Column(updatable = false)
	private String nickname;

	@Column(nullable = false, updatable = false)
	private String email;

	@Column(updatable = false)
	private String cpf;

	@Column(updatable = false)
	private String phone;

	@Column(name = "emergency_contact", updatable = false)
	private String emergencyContact;

	@Column(name = "emergency_phone", updatable = false)
	private String emergencyPhone;

	@Column(name = "birth_date", updatable = false)
	private LocalDate birthDate;

	@Column(updatable = false)
	private String belt;

	@Column(nullable = false, updatable = false)
	private short degrees;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status = Status.PENDING;

	@Column(name = "student_id")
	private UUID studentId;

	@Column(name = "decided_by")
	private UUID decidedBy;

	@Column(name = "decided_at")
	private Instant decidedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	protected EnrollmentRequest() {
	}

	EnrollmentRequest(UUID academyId, UUID userId, UUID membershipId, SignupData data) {
		this.academyId = academyId;
		this.userId = userId;
		this.membershipId = membershipId;
		this.name = data.name();
		this.nickname = data.nickname();
		this.email = data.email();
		this.cpf = data.cpf();
		this.phone = data.phone();
		this.emergencyContact = data.emergencyContact();
		this.emergencyPhone = data.emergencyPhone();
		this.birthDate = data.birthDate();
		this.belt = data.belt();
		this.degrees = (short) data.degrees();
	}

	void approve(UUID studentId, UUID by, Instant at) {
		this.status = Status.APPROVED;
		this.studentId = studentId;
		this.decidedBy = by;
		this.decidedAt = at;
	}

	void reject(UUID by, Instant at) {
		this.status = Status.REJECTED;
		this.decidedBy = by;
		this.decidedAt = at;
	}

	SignupData toSignupData() {
		return new SignupData(name, nickname, email, cpf, phone, emergencyContact, emergencyPhone, birthDate, belt,
				degrees);
	}

	public UUID getId() {
		return id;
	}

	public UUID getUserId() {
		return userId;
	}

	public UUID getMembershipId() {
		return membershipId;
	}

	public Status getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
