package br.com.academia.api.graduation;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.graduation.Graduation.Kind;
import br.com.academia.api.graduation.GraduationDtos.GraduationRequest;
import br.com.academia.api.graduation.GraduationDtos.GraduationView;
import br.com.academia.api.graduation.GraduationDtos.StudentGraduations;
import br.com.academia.api.membership.StaffService;
import br.com.academia.api.student.Belt;
import br.com.academia.api.student.Student;
import br.com.academia.api.student.StudentRepository;
import br.com.academia.api.student.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GraduationService {

	private final GraduationRepository graduations;
	private final StudentRepository studentRepository;
	private final StudentService students;
	private final ReadinessService readiness;
	private final StaffService staff;
	private final Clock clock;

	public GraduationService(GraduationRepository graduations, StudentRepository studentRepository,
			StudentService students, ReadinessService readiness, StaffService staff, Clock clock) {
		this.graduations = graduations;
		this.studentRepository = studentRepository;
		this.students = students;
		this.readiness = readiness;
		this.staff = staff;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public StudentGraduations history(UUID academyId, UUID studentId) {
		students.require(academyId, studentId);
		List<Graduation> all = graduations.findByStudentIdOrderByGraduatedOnDescCreatedAtDesc(studentId);
		Map<UUID, String> names = staff.namesById(academyId);
		UUID latestId = all.isEmpty() ? null : all.getFirst().getId();
		List<GraduationView> views = all.stream().map(g -> view(g, names, g.getId().equals(latestId))).toList();
		return new StudentGraduations(views, readiness.progress(academyId, studentId));
	}

	@Transactional
	public StudentGraduations register(UUID academyId, UUID recordedBy, UUID studentId, GraduationRequest request) {
		Student student = lock(academyId, studentId);
		student.requireTraining();
		requireValidDate(studentId, request.graduatedOn());

		Belt target = request.kind() == Kind.BELT ? Belt.fromSlug(request.belt()) : student.getBelt();
		int degrees = request.degrees();
		target.requireValidDegrees(degrees);
		requireValidSequence(student, request.kind(), target, degrees, Boolean.TRUE.equals(request.exception()));

		graduations.save(new Graduation(student, request.kind(), target, degrees, request, recordedBy));
		student.applyGraduation(target, degrees, request.graduatedOn());
		graduations.flush();
		return history(academyId, studentId);
	}

	@Transactional
	public StudentGraduations removeLatest(UUID academyId, UUID studentId, UUID graduationId) {
		Student student = lock(academyId, studentId);
		Graduation graduation = graduations.findByIdAndStudentId(graduationId, studentId)
				.orElseThrow(() -> ApiException.notFound("GRADUATION_NOT_FOUND", "Graduação não encontrada"));
		boolean latest = graduations.findFirstByStudentIdOrderByGraduatedOnDescCreatedAtDesc(studentId)
				.map(g -> g.getId().equals(graduationId))
				.orElse(false);
		if (!latest) {
			throw ApiException.conflict("GRADUATION_NOT_LATEST", "Só a graduação mais recente pode ser removida");
		}
		graduation.revert(student);
		graduations.delete(graduation);
		graduations.flush();
		return history(academyId, studentId);
	}

	private Student lock(UUID academyId, UUID studentId) {
		return studentRepository.findForUpdate(studentId, academyId)
				.orElseThrow(() -> ApiException.notFound("STUDENT_NOT_FOUND", "Aluno não encontrado"));
	}

	private void requireValidDate(UUID studentId, LocalDate date) {
		if (date.isAfter(LocalDate.now(clock))) {
			throw ApiException.badRequest("GRADUATION_IN_FUTURE", "A data da graduação não pode ser futura");
		}
		graduations.findFirstByStudentIdOrderByGraduatedOnDescCreatedAtDesc(studentId)
				.filter(last -> date.isBefore(last.getGraduatedOn()))
				.ifPresent(last -> {
					throw ApiException.badRequest("GRADUATION_BEFORE_LAST",
							"A data não pode ser anterior à última graduação (" + last.getGraduatedOn() + ")");
				});
	}

	/** Sem exceção do gestor: nova faixa acima da atual, ou grau acima do atual na mesma faixa. */
	private static void requireValidSequence(Student s, Kind kind, Belt target, int degrees, boolean exception) {
		boolean unchanged = target == s.getBelt() && degrees == s.getDegrees();
		boolean forward = kind == Kind.BELT ? target.compareTo(s.getBelt()) > 0 : degrees > s.getDegrees();
		if (unchanged || (!exception && !forward)) {
			throw ApiException.badRequest("INVALID_GRADUATION_SEQUENCE",
					"A graduação precisa avançar a faixa ou o grau atual do aluno");
		}
	}

	private static GraduationView view(Graduation g, Map<UUID, String> names, boolean removable) {
		String recordedBy = g.getRecordedBy() == null ? null : names.get(g.getRecordedBy());
		return new GraduationView(g.getId(), g.getKind(), g.getBelt().slug(), g.getDegrees(), g.getGraduatedOn(),
				g.getPriorTrainings(), g.isException(), g.getNote(), recordedBy, removable);
	}
}
