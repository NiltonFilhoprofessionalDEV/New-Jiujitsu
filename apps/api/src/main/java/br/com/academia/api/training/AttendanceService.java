package br.com.academia.api.training;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.TextNormalizer;
import br.com.academia.api.membership.StaffService;
import br.com.academia.api.student.Student;
import br.com.academia.api.student.StudentEnums.Status;
import br.com.academia.api.student.StudentRepository;
import br.com.academia.api.training.AttendanceDtos.DayClass;
import br.com.academia.api.training.AttendanceDtos.RosterEntry;
import br.com.academia.api.training.AttendanceDtos.SaveSessionRequest;
import br.com.academia.api.training.AttendanceDtos.SessionView;
import br.com.academia.api.training.ClassDtos.ClassStudent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceService {

	private static final Comparator<RosterEntry> BY_NAME =
			Comparator.comparing(e -> TextNormalizer.fold(e.student().name()));

	private final ClassService classService;
	private final TrainingClassRepository classes;
	private final ClassEnrollmentRepository enrollments;
	private final ClassSessionRepository sessions;
	private final CheckInRepository checkIns;
	private final StudentRepository students;
	private final StaffService staff;
	private final Clock clock;

	public AttendanceService(ClassService classService, TrainingClassRepository classes,
			ClassEnrollmentRepository enrollments, ClassSessionRepository sessions, CheckInRepository checkIns,
			StudentRepository students, StaffService staff, Clock clock) {
		this.classService = classService;
		this.classes = classes;
		this.enrollments = enrollments;
		this.sessions = sessions;
		this.checkIns = checkIns;
		this.students = students;
		this.staff = staff;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public List<DayClass> day(UUID academyId, LocalDate date) {
		List<TrainingClass> ofDay = classes.findActive(academyId).stream().filter(c -> c.occursOn(date)).toList();
		if (ofDay.isEmpty()) {
			return List.of();
		}
		Map<UUID, Long> enrolled = toMap(enrollments.countByClassIds(ofDay.stream().map(TrainingClass::getId).toList()));
		Map<UUID, Long> present = toMap(checkIns.countPresentByClass(academyId, date));
		Map<UUID, String> names = staff.namesById(academyId);
		return ofDay.stream().map(c -> new DayClass(c.getId(), c.getTitle(), c.getStartTime(), c.getEndTime(),
				c.getLocation(), ClassService.instructorOf(c, names), enrolled.getOrDefault(c.getId(), 0L),
				present.getOrDefault(c.getId(), 0L))).toList();
	}

	@Transactional(readOnly = true)
	public SessionView session(UUID academyId, UUID classId, LocalDate date) {
		TrainingClass trainingClass = requireOn(academyId, classId, date);
		return view(academyId, trainingClass, date);
	}

	@Transactional
	public SessionView save(UUID academyId, UUID markedBy, UUID classId, LocalDate date, SaveSessionRequest request) {
		TrainingClass trainingClass = requireOn(academyId, classId, date);
		Set<UUID> present = new LinkedHashSet<>(request.presentStudentIds());
		if (!present.isEmpty() && date.isAfter(LocalDate.now(clock))) {
			throw ApiException.badRequest("ATTENDANCE_IN_FUTURE", "Só é possível marcar presença até hoje");
		}
		requireTrainingStudents(academyId, present);

		sessions.insertIfAbsent(academyId, classId, date);
		ClassSession session = sessions.findByClassIdAndSessionDate(classId, date).orElseThrow();
		session.setNotes(request.notes());

		Set<UUID> existing = new LinkedHashSet<>(checkIns.findStudentIds(session.getId()));
		List<UUID> removed = existing.stream().filter(id -> !present.contains(id)).toList();
		if (!removed.isEmpty()) {
			checkIns.deleteStudents(session.getId(), removed);
		}
		present.stream().filter(id -> !existing.contains(id))
				.forEach(id -> checkIns.insertIfAbsent(session.getId(), id, markedBy));
		return view(academyId, trainingClass, date);
	}

	private TrainingClass requireOn(UUID academyId, UUID classId, LocalDate date) {
		TrainingClass trainingClass = classService.require(academyId, classId);
		if (!trainingClass.occursOn(date)) {
			throw ApiException.badRequest("CLASS_NOT_ON_DATE", "Esta aula não acontece nesta data");
		}
		return trainingClass;
	}

	private void requireTrainingStudents(UUID academyId, Set<UUID> ids) {
		if (ids.isEmpty()) {
			return;
		}
		List<Student> found = students.findAllInAcademy(academyId, new ArrayList<>(ids));
		if (found.size() != ids.size()) {
			throw ApiException.notFound("STUDENT_NOT_FOUND", "Aluno não encontrado");
		}
		found.forEach(Student::requireTraining);
	}

	/** Inscritos ativos mais quem foi marcado na data (avulsos e inscritos que saíram depois). */
	private SessionView view(UUID academyId, TrainingClass c, LocalDate date) {
		ClassSession session = sessions.findByClassIdAndSessionDate(c.getId(), date).orElse(null);
		Set<UUID> presentIds = session == null ? Set.of() : Set.copyOf(checkIns.findStudentIds(session.getId()));

		List<RosterEntry> roster = new ArrayList<>();
		Set<UUID> listed = new LinkedHashSet<>();
		for (Student s : enrollments.findEnrolledStudents(c.getId())) {
			if (s.getStatus() == Status.ACTIVE || presentIds.contains(s.getId())) {
				roster.add(new RosterEntry(ClassStudent.of(s), true, presentIds.contains(s.getId())));
				listed.add(s.getId());
			}
		}
		List<UUID> walkIns = presentIds.stream().filter(id -> !listed.contains(id)).toList();
		if (!walkIns.isEmpty()) {
			students.findAllInAcademy(academyId, walkIns)
					.forEach(s -> roster.add(new RosterEntry(ClassStudent.of(s), false, true)));
		}
		roster.sort(BY_NAME);

		return new SessionView(c.getId(), c.getTitle(), date, c.getStartTime(), c.getEndTime(),
				ClassService.instructorOf(c, staff.namesById(academyId)), session == null ? null : session.getNotes(),
				!date.isAfter(LocalDate.now(clock)), roster);
	}

	private static Map<UUID, Long> toMap(List<ClassCount> counts) {
		return counts.stream().collect(Collectors.toMap(ClassCount::classId, ClassCount::total));
	}
}
