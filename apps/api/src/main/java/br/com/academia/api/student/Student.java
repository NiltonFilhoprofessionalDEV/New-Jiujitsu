package br.com.academia.api.student;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.TextNormalizer;
import br.com.academia.api.student.StudentEnums.BeltRuler;
import br.com.academia.api.student.StudentEnums.Gender;
import br.com.academia.api.student.StudentEnums.Kind;
import br.com.academia.api.student.StudentEnums.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "students")
public class Student {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "academy_id", nullable = false, updatable = false)
	private UUID academyId;

	@Column(name = "user_id")
	private UUID userId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "household_id")
	private Household household;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Kind kind;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status = Status.ACTIVE;

	@Column(nullable = false)
	private String name;

	private String nickname;

	@Column(name = "search_name", nullable = false)
	private String searchName;

	private String email;
	private String cpf;
	private String phone;

	@Column(name = "emergency_contact")
	private String emergencyContact;

	@Column(name = "emergency_phone")
	private String emergencyPhone;

	@Column(name = "birth_date")
	private LocalDate birthDate;

	@Enumerated(EnumType.STRING)
	private Gender gender;

	private Belt belt;

	@Column(nullable = false)
	private short degrees;

	@Column(name = "degree_awarded_at")
	private LocalDate degreeAwardedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "belt_ruler", nullable = false)
	private BeltRuler beltRuler = BeltRuler.AUTO;

	@Column(name = "enrollment_date", nullable = false)
	private LocalDate enrollmentDate;

	@Column(name = "gi_size")
	private String giSize;

	@Column(name = "belt_size")
	private String beltSize;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt = Instant.now();

	protected Student() {
	}

	public Student(UUID academyId) {
		this.academyId = academyId;
	}

	@PreUpdate
	void touch() {
		updatedAt = Instant.now();
	}

	/** Recebe dados já normalizados e validados pelo {@link StudentService}. */
	void apply(StudentData data) {
		this.kind = data.kind();
		this.name = data.name();
		this.nickname = data.nickname();
		this.searchName = TextNormalizer.fold(data.name() + " " + (data.nickname() == null ? "" : data.nickname()));
		this.email = data.email();
		this.cpf = data.cpf();
		this.phone = data.phone();
		this.emergencyContact = data.emergencyContact();
		this.emergencyPhone = data.emergencyPhone();
		this.birthDate = data.birthDate();
		this.gender = data.gender();
		this.belt = data.belt();
		this.degrees = (short) data.degrees();
		this.degreeAwardedAt = data.degreeAwardedAt();
		this.beltRuler = data.beltRuler();
		this.enrollmentDate = data.enrollmentDate();
		this.giSize = data.giSize();
		this.beltSize = data.beltSize();
	}

	/** A tela não edita mais esses campos; uma atualização não apaga o que já estava salvo. */
	public void keepEquipmentSizes(String giSize, String beltSize) {
		this.giSize = giSize;
		this.beltSize = beltSize;
	}

	/** Graduação registrada (ou desfeita): só faixa, grau e data mudam; o histórico fica em {@code graduations}. */
	public void applyGraduation(Belt belt, int degrees, LocalDate awardedAt) {
		belt.requireValidDegrees(degrees);
		this.belt = belt;
		this.degrees = (short) degrees;
		this.degreeAwardedAt = awardedAt;
	}

	public void requireTraining() {
		if (kind != Kind.TRAINING) {
			throw ApiException.badRequest("STUDENT_NOT_TRAINING", name + " é responsável e não treina");
		}
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setHousehold(Household household) {
		this.household = household;
	}

	void linkUser(UUID userId) {
		this.userId = userId;
	}

	public UUID getId() {
		return id;
	}

	public UUID getAcademyId() {
		return academyId;
	}

	public UUID getUserId() {
		return userId;
	}

	public Household getHousehold() {
		return household;
	}

	public Kind getKind() {
		return kind;
	}

	public Status getStatus() {
		return status;
	}

	public String getName() {
		return name;
	}

	public String getNickname() {
		return nickname;
	}

	public String getEmail() {
		return email;
	}

	public String getCpf() {
		return cpf;
	}

	public String getPhone() {
		return phone;
	}

	public String getEmergencyContact() {
		return emergencyContact;
	}

	public String getEmergencyPhone() {
		return emergencyPhone;
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public Gender getGender() {
		return gender;
	}

	public Belt getBelt() {
		return belt;
	}

	public int getDegrees() {
		return degrees;
	}

	public LocalDate getDegreeAwardedAt() {
		return degreeAwardedAt;
	}

	public BeltRuler getBeltRuler() {
		return beltRuler;
	}

	public LocalDate getEnrollmentDate() {
		return enrollmentDate;
	}

	public String getGiSize() {
		return giSize;
	}

	public String getBeltSize() {
		return beltSize;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
