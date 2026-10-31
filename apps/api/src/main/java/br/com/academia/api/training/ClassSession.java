package br.com.academia.api.training;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import br.com.academia.api.common.TextNormalizer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/** Uma data concreta de uma aula; criada na primeira vez que a chamada é salva. */
@Entity
@Table(name = "class_sessions")
public class ClassSession {

	@Id
	private UUID id;

	@Column(name = "academy_id", nullable = false, updatable = false)
	private UUID academyId;

	@Column(name = "class_id", nullable = false, updatable = false)
	private UUID classId;

	@Column(name = "session_date", nullable = false, updatable = false)
	private LocalDate sessionDate;

	/** Fora da grade semanal: criada pelo professor como aula extra. */
	@Column(nullable = false, updatable = false)
	private boolean extra;

	private String notes;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected ClassSession() {
	}

	@PreUpdate
	void touch() {
		updatedAt = Instant.now();
	}

	void setNotes(String notes) {
		this.notes = TextNormalizer.blankToNull(notes);
	}

	public UUID getId() {
		return id;
	}

	public UUID getClassId() {
		return classId;
	}

	public LocalDate getSessionDate() {
		return sessionDate;
	}

	public String getNotes() {
		return notes;
	}

	public boolean isExtra() {
		return extra;
	}
}
