package br.com.academia.api.graduation;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import br.com.academia.api.common.TextNormalizer;
import br.com.academia.api.student.Belt;
import br.com.academia.api.student.Student;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "graduations")
public class Graduation {

	public enum Kind {
		BELT, DEGREE
	}

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "academy_id", nullable = false, updatable = false)
	private UUID academyId;

	@Column(name = "student_id", nullable = false, updatable = false)
	private UUID studentId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Kind kind;

	@Column(nullable = false)
	private Belt belt;

	@Column(nullable = false)
	private short degrees;

	@Column(name = "previous_belt")
	private Belt previousBelt;

	@Column(name = "previous_degrees", nullable = false)
	private short previousDegrees;

	@Column(name = "previous_awarded_at")
	private LocalDate previousAwardedAt;

	@Column(name = "graduated_on", nullable = false)
	private LocalDate graduatedOn;

	@Column(name = "prior_trainings")
	private Integer priorTrainings;

	@Column(nullable = false)
	private boolean exception;

	private String note;

	@Column(name = "recorded_by")
	private UUID recordedBy;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	protected Graduation() {
	}

	/** Captura o estado atual do aluno como "anterior" antes de a graduação ser aplicada. */
	Graduation(Student student, Kind kind, Belt belt, int degrees, GraduationDtos.GraduationRequest request,
			UUID recordedBy) {
		this.academyId = student.getAcademyId();
		this.studentId = student.getId();
		this.kind = kind;
		this.belt = belt;
		this.degrees = (short) degrees;
		this.previousBelt = student.getBelt();
		this.previousDegrees = (short) student.getDegrees();
		this.previousAwardedAt = student.getDegreeAwardedAt();
		this.graduatedOn = request.graduatedOn();
		this.priorTrainings = request.priorTrainings();
		this.exception = Boolean.TRUE.equals(request.exception());
		this.note = TextNormalizer.blankToNull(request.note());
		this.recordedBy = recordedBy;
	}

	/** Devolve ao aluno a faixa e o grau que ele tinha antes desta graduação. */
	void revert(Student student) {
		student.applyGraduation(previousBelt, previousDegrees, previousAwardedAt);
	}

	public UUID getId() {
		return id;
	}

	public UUID getStudentId() {
		return studentId;
	}

	public Kind getKind() {
		return kind;
	}

	public Belt getBelt() {
		return belt;
	}

	public int getDegrees() {
		return degrees;
	}

	public LocalDate getGraduatedOn() {
		return graduatedOn;
	}

	public Integer getPriorTrainings() {
		return priorTrainings;
	}

	public boolean isException() {
		return exception;
	}

	public String getNote() {
		return note;
	}

	public UUID getRecordedBy() {
		return recordedBy;
	}
}
