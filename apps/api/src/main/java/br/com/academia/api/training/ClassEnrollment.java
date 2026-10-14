package br.com.academia.api.training;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Aluno inscrito na turma: aparece na chamada de todas as datas da aula. */
@Entity
@Table(name = "class_enrollments")
public class ClassEnrollment {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "class_id", nullable = false, updatable = false)
	private UUID classId;

	@Column(name = "student_id", nullable = false, updatable = false)
	private UUID studentId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	protected ClassEnrollment() {
	}

	public ClassEnrollment(UUID classId, UUID studentId) {
		this.classId = classId;
		this.studentId = studentId;
	}

	public UUID getClassId() {
		return classId;
	}

	public UUID getStudentId() {
		return studentId;
	}
}
