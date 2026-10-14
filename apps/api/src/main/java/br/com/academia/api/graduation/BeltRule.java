package br.com.academia.api.graduation;

import java.time.Instant;
import java.util.UUID;

import br.com.academia.api.student.Belt;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "belt_rules")
public class BeltRule {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "academy_id", nullable = false, updatable = false)
	private UUID academyId;

	@Column(nullable = false, updatable = false)
	private Belt belt;

	@Column(name = "min_months")
	private Integer minMonths;

	@Column(name = "min_trainings")
	private Integer minTrainings;

	@Column(name = "trainings_per_degree")
	private Integer trainingsPerDegree;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt = Instant.now();

	protected BeltRule() {
	}

	BeltRule(UUID academyId, Belt belt) {
		this.academyId = academyId;
		this.belt = belt;
	}

	@PreUpdate
	void touch() {
		updatedAt = Instant.now();
	}

	void apply(RuleValues values) {
		this.minMonths = values.minMonths();
		this.minTrainings = values.minTrainings();
		this.trainingsPerDegree = values.trainingsPerDegree();
	}

	Belt getBelt() {
		return belt;
	}

	RuleValues values() {
		return new RuleValues(minMonths, minTrainings, trainingsPerDegree);
	}
}
