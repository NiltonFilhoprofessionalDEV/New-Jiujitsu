package br.com.academia.api.student;

import java.time.Instant;
import java.util.UUID;

import br.com.academia.api.common.TextNormalizer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "households")
public class Household {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "academy_id", nullable = false, updatable = false)
	private UUID academyId;

	@Column(nullable = false)
	private String name;

	@Column(name = "search_name", nullable = false)
	private String searchName;

	@Column(name = "responsible_student_id")
	private UUID responsibleStudentId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt = Instant.now();

	protected Household() {
	}

	public Household(UUID academyId, String name) {
		this.academyId = academyId;
		rename(name);
	}

	@PreUpdate
	void touch() {
		updatedAt = Instant.now();
	}

	public void rename(String name) {
		this.name = name.trim();
		this.searchName = TextNormalizer.fold(name);
	}

	public void setResponsibleStudentId(UUID studentId) {
		this.responsibleStudentId = studentId;
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

	public UUID getResponsibleStudentId() {
		return responsibleStudentId;
	}
}
