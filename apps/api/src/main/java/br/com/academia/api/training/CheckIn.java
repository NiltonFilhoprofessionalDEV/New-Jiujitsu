package br.com.academia.api.training;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Presença de um aluno numa sessão. Gravada por SQL nativo idempotente (ver {@link CheckInRepository}). */
@Entity
@Table(name = "check_ins")
public class CheckIn {

	@Id
	private UUID id;

	@Column(name = "session_id", nullable = false, updatable = false)
	private UUID sessionId;

	@Column(name = "student_id", nullable = false, updatable = false)
	private UUID studentId;

	@Column(name = "marked_by")
	private UUID markedBy;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected CheckIn() {
	}

	public UUID getSessionId() {
		return sessionId;
	}

	public UUID getStudentId() {
		return studentId;
	}

	public UUID getMarkedBy() {
		return markedBy;
	}
}
