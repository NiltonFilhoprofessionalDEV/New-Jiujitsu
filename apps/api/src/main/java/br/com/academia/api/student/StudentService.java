package br.com.academia.api.student;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.Cpf;
import br.com.academia.api.common.TextNormalizer;
import br.com.academia.api.student.StudentDtos.StudentCounts;
import br.com.academia.api.student.StudentDtos.StudentDetail;
import br.com.academia.api.student.StudentDtos.StudentListItem;
import br.com.academia.api.student.StudentDtos.StudentRequest;
import br.com.academia.api.student.StudentEnums.BeltRuler;
import br.com.academia.api.student.StudentEnums.Kind;
import br.com.academia.api.student.StudentEnums.Status;
import br.com.academia.api.student.StudentFilter.KindFilter;
import br.com.academia.api.student.StudentFilter.StatusFilter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {

	private static final String DEFAULT_BELT = "branca";

	private final StudentRepository students;
	private final ApplicationEventPublisher events;
	private final Clock clock;

	public StudentService(StudentRepository students, ApplicationEventPublisher events, Clock clock) {
		this.students = students;
		this.events = events;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public Page<StudentListItem> page(UUID academyId, StudentFilter filter, Pageable pageable) {
		return students.findAll(StudentSpecs.matching(academyId, filter, today()), pageable).map(StudentListItem::of);
	}

	@Transactional(readOnly = true)
	public StudentCounts counts(UUID academyId, StudentFilter filter) {
		long all = count(academyId, filter.withKind(KindFilter.ALL));
		long training = count(academyId, filter.withKind(KindFilter.TRAINING));
		long responsible = count(academyId, filter.withKind(KindFilter.RESPONSIBLE));
		long hiddenInactive = filter.status() == StatusFilter.ACTIVE
				? count(academyId, filter.withStatus(StatusFilter.INACTIVE))
				: 0;
		return new StudentCounts(all, training, responsible, hiddenInactive);
	}

	@Transactional(readOnly = true)
	public StudentDetail get(UUID academyId, UUID id) {
		return StudentDetail.of(require(academyId, id));
	}

	/** {@code household} preenchido cadastra o aluno já como dependente daquela família. */
	@Transactional
	public StudentDetail create(UUID academyId, StudentRequest request, Household household) {
		return saveNew(academyId, request, household, null, true);
	}

	/**
	 * Liga o aluno à conta de login. O cadastro público pode chegar sem a data da faixa; o gestor confirma
	 * depois, na aprovação ou na ficha.
	 */
	@Transactional
	public StudentDetail create(UUID academyId, StudentRequest request, Household household, UUID userId) {
		return saveNew(academyId, request, household, userId, false);
	}

	/** Faixa e datas confirmadas pelo gestor na aprovação da matrícula. */
	@Transactional
	public StudentDetail confirmRank(UUID academyId, UUID studentId, String beltSlug, int degrees,
			LocalDate beltAwardedAt, LocalDate degreeAwardedAt) {
		Student student = require(academyId, studentId);
		student.requireTraining();
		Belt belt = Belt.fromSlug(beltSlug == null || beltSlug.isBlank() ? DEFAULT_BELT : beltSlug);
		belt.requireValidDegrees(degrees);
		GraduationDates.RankDates rank = GraduationDates.resolve(belt, degrees, beltAwardedAt, degreeAwardedAt, true);
		student.confirmRank(belt, degrees, rank.beltAwardedAt(), rank.degreeAwardedAt());
		return StudentDetail.of(student);
	}

	private StudentDetail saveNew(UUID academyId, StudentRequest request, Household household, UUID userId,
			boolean requireGraduationDates) {
		Student student = new Student(academyId);
		student.apply(normalize(academyId, request, null, requireGraduationDates));
		student.setHousehold(household);
		student.linkUser(userId);
		students.save(student);
		events.publishEvent(new StudentCreatedEvent(student));
		return StudentDetail.of(student);
	}

	/** Falha cedo, antes de criar conta, se e-mail ou CPF já pertencem a outro aluno da academia. */
	@Transactional(readOnly = true)
	public void ensureAvailable(UUID academyId, String email, String cpf) {
		ensureUnique(academyId, email == null ? null : email.trim().toLowerCase(Locale.ROOT), Cpf.normalize(cpf), null);
	}

	@Transactional
	public StudentDetail update(UUID academyId, UUID id, StudentRequest request) {
		Student student = require(academyId, id);
		String giSize = student.getGiSize();
		String beltSize = student.getBeltSize();
		student.apply(normalize(academyId, request, id, true));
		student.keepEquipmentSizes(giSize, beltSize);
		return StudentDetail.of(student);
	}

	@Transactional
	public StudentDetail setStatus(UUID academyId, UUID id, Status status) {
		Student student = require(academyId, id);
		student.setStatus(status);
		return StudentDetail.of(student);
	}

	@Transactional
	public void delete(UUID academyId, UUID id) {
		Student student = require(academyId, id);
		events.publishEvent(new StudentDeletingEvent(student));
		students.delete(student);
	}

	public Student require(UUID academyId, UUID id) {
		return students.findInAcademy(id, academyId)
				.orElseThrow(() -> ApiException.notFound("STUDENT_NOT_FOUND", "Aluno não encontrado"));
	}

	private long count(UUID academyId, StudentFilter filter) {
		return students.count(StudentSpecs.matching(academyId, filter, today()));
	}

	private StudentData normalize(UUID academyId, StudentRequest r, UUID exceptId, boolean requireGraduationDates) {
		StudentRules.Person person = StudentRules.person(r.kind(),
				new StudentRules.PersonInput(r.name(), r.phone(), r.emergencyContact(), r.emergencyPhone()));
		String email = r.email() == null || r.email().isBlank() ? null : r.email().trim().toLowerCase(Locale.ROOT);
		String cpf = Cpf.normalize(r.cpf());
		ensureUnique(academyId, email, cpf, exceptId);

		boolean training = r.kind() == Kind.TRAINING;
		Belt belt = training ? Belt.fromSlug(r.belt() == null || r.belt().isBlank() ? DEFAULT_BELT : r.belt()) : null;
		int degrees = 0;
		if (belt != null) {
			degrees = r.degrees() == null ? belt.minDegrees() : r.degrees();
			belt.requireValidDegrees(degrees);
		}
		GraduationDates.RankDates rank = GraduationDates.resolve(belt, degrees, r.beltAwardedAt(), r.degreeAwardedAt(),
				requireGraduationDates && training);
		return new StudentData(
				r.kind(),
				person.name(),
				TextNormalizer.blankToNull(r.nickname()),
				email,
				cpf,
				person.phone(),
				person.emergencyContact(),
				person.emergencyPhone(),
				r.birthDate(),
				r.gender(),
				belt,
				degrees,
				training ? rank.degreeAwardedAt() : null,
				training ? rank.beltAwardedAt() : null,
				r.beltRuler() == null ? BeltRuler.AUTO : r.beltRuler(),
				r.enrollmentDate() == null ? today() : r.enrollmentDate(),
				training ? TextNormalizer.blankToNull(r.giSize()) : null,
				training ? TextNormalizer.blankToNull(r.beltSize()) : null);
	}

	private void ensureUnique(UUID academyId, String email, String cpf, UUID exceptId) {
		boolean emailTaken = email != null && (exceptId == null
				? students.existsByAcademyIdAndEmail(academyId, email)
				: students.existsByAcademyIdAndEmailAndIdNot(academyId, email, exceptId));
		if (emailTaken) {
			throw ApiException.conflict("STUDENT_EMAIL_IN_USE", "Já existe um aluno com este e-mail");
		}
		boolean cpfTaken = cpf != null && (exceptId == null
				? students.existsByAcademyIdAndCpf(academyId, cpf)
				: students.existsByAcademyIdAndCpfAndIdNot(academyId, cpf, exceptId));
		if (cpfTaken) {
			throw ApiException.conflict("STUDENT_CPF_IN_USE", "Já existe um aluno com este CPF");
		}
	}

	private LocalDate today() {
		return LocalDate.now(clock);
	}
}
