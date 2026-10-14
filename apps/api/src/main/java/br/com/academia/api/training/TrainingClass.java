package br.com.academia.api.training;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.academia.api.common.TextNormalizer;
import br.com.academia.api.training.ClassDtos.ClassRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Aula semanal recorrente. Dias seguem a ISO-8601: 1 = segunda ... 7 = domingo. */
@Entity
@Table(name = "training_classes")
public class TrainingClass {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "academy_id", nullable = false, updatable = false)
	private UUID academyId;

	@Column(nullable = false)
	private String title;

	private String description;

	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(nullable = false, columnDefinition = "integer[]")
	private List<Integer> weekdays = new ArrayList<>();

	@Column(name = "start_time", nullable = false)
	private LocalTime startTime;

	@Column(name = "end_time")
	private LocalTime endTime;

	private String location;

	@Column(name = "instructor_membership_id")
	private UUID instructorMembershipId;

	private Integer capacity;

	@Column(name = "archived_at")
	private Instant archivedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt = Instant.now();

	protected TrainingClass() {
	}

	public TrainingClass(UUID academyId, ClassRequest request) {
		this.academyId = academyId;
		apply(request);
	}

	@PreUpdate
	void touch() {
		updatedAt = Instant.now();
	}

	/** Campos já validados pelo service (horário e professor). */
	void apply(ClassRequest r) {
		this.title = r.title().trim();
		this.description = TextNormalizer.blankToNull(r.description());
		this.weekdays = new ArrayList<>(r.weekdays().stream().distinct().sorted().toList());
		this.startTime = r.startTime();
		this.endTime = r.endTime();
		this.location = TextNormalizer.blankToNull(r.location());
		this.instructorMembershipId = r.instructorMembershipId();
		this.capacity = r.capacity();
	}

	void archive() {
		this.archivedAt = Instant.now();
	}

	boolean occursOn(LocalDate date) {
		return weekdays.contains(date.getDayOfWeek().getValue());
	}

	boolean isArchived() {
		return archivedAt != null;
	}

	public UUID getId() {
		return id;
	}

	public UUID getAcademyId() {
		return academyId;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public List<Integer> getWeekdays() {
		return weekdays;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public String getLocation() {
		return location;
	}

	public UUID getInstructorMembershipId() {
		return instructorMembershipId;
	}

	public Integer getCapacity() {
		return capacity;
	}
}
