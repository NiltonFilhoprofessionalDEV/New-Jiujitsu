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
		return create(academyId, request, household, null);
	}

	/** {@code userId} preenchido liga o aluno à conta de login dele (convite aceito ou cadastro aprovado). */
	@Transactional
	public StudentDetail create(UUID academyId, StudentRequest request, Household household, UUID userId) {
		Student student = new Student(academyId);
		student.apply(normalize(academyId, request, null));
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
		student.apply(normalize(academyId, request, id));
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

	private StudentData normalize(UUID academyId, StudentRequest r, UUID exceptId) {
		StudentRules.Person person = StudentRules.person(r.kind(), r.name(), r.phone(), r.emergencyPhone());
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
		return new StudentData(
				r.kind(),
				person.name(),
				TextNormalizer.blankToNull(r.nickname()),
				email,
				cpf,
				person.phone(),
				person.emergencyPhone(),
				r.birthDate(),
				r.gender(),
				belt,
				degrees,
				training ? r.degreeAwardedAt() : null,
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
