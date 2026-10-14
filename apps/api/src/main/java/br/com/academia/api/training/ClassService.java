package br.com.academia.api.training;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.membership.StaffService;
import br.com.academia.api.student.Student;
import br.com.academia.api.student.StudentEnums.Status;
import br.com.academia.api.student.StudentService;
import br.com.academia.api.training.ClassDtos.ClassRequest;
import br.com.academia.api.training.ClassDtos.ClassStudent;
import br.com.academia.api.training.ClassDtos.ClassView;
import br.com.academia.api.training.ClassDtos.StaffRef;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClassService {

	private final TrainingClassRepository classes;
	private final ClassEnrollmentRepository enrollments;
	private final StudentService studentService;
	private final StaffService staff;

	public ClassService(TrainingClassRepository classes, ClassEnrollmentRepository enrollments,
			StudentService studentService, StaffService staff) {
		this.classes = classes;
		this.enrollments = enrollments;
		this.studentService = studentService;
		this.staff = staff;
	}

	@Transactional(readOnly = true)
	public List<ClassView> list(UUID academyId) {
		List<TrainingClass> all = classes.findActive(academyId);
		if (all.isEmpty()) {
			return List.of();
		}
		Map<UUID, Long> counts = enrollments.countByClassIds(all.stream().map(TrainingClass::getId).toList())
				.stream().collect(Collectors.toMap(ClassCount::classId, ClassCount::total));
		Map<UUID, String> names = staff.namesById(academyId);
		return all.stream().map(c -> view(c, names, counts.getOrDefault(c.getId(), 0L))).toList();
	}

	@Transactional
	public ClassView create(UUID academyId, ClassRequest request) {
		validate(academyId, request);
		TrainingClass created = classes.save(new TrainingClass(academyId, request));
		return view(created, staff.namesById(academyId), 0);
	}

	@Transactional
	public ClassView update(UUID academyId, UUID id, ClassRequest request) {
		TrainingClass trainingClass = require(academyId, id);
		validate(academyId, request);
		trainingClass.apply(request);
		return view(trainingClass, staff.namesById(academyId), enrollments.countByClassId(id));
	}

	/** Arquiva em vez de apagar: a frequência já registrada continua valendo para graduação e relatórios. */
	@Transactional
	public void archive(UUID academyId, UUID id) {
		require(academyId, id).archive();
	}

	@Transactional(readOnly = true)
	public List<ClassStudent> students(UUID academyId, UUID id) {
		require(academyId, id);
		return enrolledStudents(id);
	}

	@Transactional
	public List<ClassStudent> enroll(UUID academyId, UUID id, UUID studentId) {
		TrainingClass trainingClass = classes.findForUpdate(id, academyId).orElseThrow(ClassService::notFound);
		Student student = studentService.require(academyId, studentId);
		student.requireTraining();
		if (student.getStatus() != Status.ACTIVE) {
			throw ApiException.badRequest("STUDENT_INACTIVE", "Reative a matrícula do aluno antes de inscrevê-lo");
		}
		if (enrollments.existsByClassIdAndStudentId(id, studentId)) {
			throw ApiException.conflict("ALREADY_ENROLLED", "O aluno já está inscrito nesta aula");
		}
		Integer capacity = trainingClass.getCapacity();
		if (capacity != null && enrollments.countByClassId(id) >= capacity) {
			throw ApiException.conflict("CLASS_FULL", "A aula atingiu a capacidade máxima");
		}
		enrollments.save(new ClassEnrollment(id, studentId));
		return enrolledStudents(id);
	}

	@Transactional
	public List<ClassStudent> unenroll(UUID academyId, UUID id, UUID studentId) {
		require(academyId, id);
		enrollments.deleteEnrollment(id, studentId);
		return enrolledStudents(id);
	}

	TrainingClass require(UUID academyId, UUID id) {
		return classes.findActiveInAcademy(id, academyId).orElseThrow(ClassService::notFound);
	}

	static StaffRef instructorOf(TrainingClass c, Map<UUID, String> staffNames) {
		UUID id = c.getInstructorMembershipId();
		String name = id == null ? null : staffNames.get(id);
		return name == null ? null : new StaffRef(id, name);
	}

	private List<ClassStudent> enrolledStudents(UUID classId) {
		return enrollments.findEnrolledStudents(classId).stream().map(ClassStudent::of).toList();
	}

	private void validate(UUID academyId, ClassRequest r) {
		if (r.endTime() != null && !r.endTime().isAfter(r.startTime())) {
			throw ApiException.badRequest("INVALID_CLASS_TIME", "O término precisa ser depois do início");
		}
		if (r.instructorMembershipId() != null) {
			staff.requireStaff(academyId, r.instructorMembershipId());
		}
	}

	private static ClassView view(TrainingClass c, Map<UUID, String> staffNames, long enrolled) {
		return new ClassView(c.getId(), c.getTitle(), c.getDescription(), c.getWeekdays(), c.getStartTime(),
				c.getEndTime(), c.getLocation(), instructorOf(c, staffNames), c.getCapacity(), enrolled);
	}

	private static ApiException notFound() {
		return ApiException.notFound("CLASS_NOT_FOUND", "Aula não encontrada");
	}
}
