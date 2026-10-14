package br.com.academia.api.training;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.membership.StaffService;
import br.com.academia.api.student.Student;
import br.com.academia.api.student.StudentEnums.Status;
import br.com.academia.api.training.ScheduleDtos.AttendanceItem;
import br.com.academia.api.training.ScheduleDtos.ScheduledClass;
import br.com.academia.api.training.ScheduleDtos.TodayClass;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Aulas e check-in pelo próprio aluno. O check-in vale nas aulas de hoje, de 1h antes do início até o fim
 * (aula sem término: 90 min). O professor continua podendo corrigir tudo na chamada.
 */
@Service
public class StudentScheduleService {

	static final Duration OPENS_BEFORE = Duration.ofHours(1);
	static final Duration DEFAULT_LENGTH = Duration.ofMinutes(90);

	private final TrainingClassRepository classes;
	private final ClassEnrollmentRepository enrollments;
	private final ClassSessionRepository sessions;
	private final CheckInRepository checkIns;
	private final StaffService staff;
	private final Clock clock;

	public StudentScheduleService(TrainingClassRepository classes, ClassEnrollmentRepository enrollments,
			ClassSessionRepository sessions, CheckInRepository checkIns, StaffService staff, Clock clock) {
		this.classes = classes;
		this.enrollments = enrollments;
		this.sessions = sessions;
		this.checkIns = checkIns;
		this.staff = staff;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public List<ScheduledClass> enrolledClasses(Student student) {
		Set<UUID> enrolled = Set.copyOf(enrollments.findClassIds(student.getId()));
		Map<UUID, String> names = staff.namesById(student.getAcademyId());
		return classes.findActive(student.getAcademyId()).stream()
				.filter(c -> enrolled.contains(c.getId()))
				.map(c -> new ScheduledClass(c.getId(), c.getTitle(), c.getWeekdays(), c.getStartTime(),
						c.getEndTime(), c.getLocation(), ClassService.instructorOf(c, names)))
				.toList();
	}

	@Transactional(readOnly = true)
	public List<AttendanceItem> recentAttendance(Student student, int limit) {
		return checkIns.findRecent(student.getId(), Limit.of(limit));
	}

	@Transactional(readOnly = true)
	public long attendanceCount(Student student) {
		return checkIns.countByStudentId(student.getId());
	}

	/** Aulas de hoje: as inscritas primeiro, depois as demais (o aluno pode treinar como avulso). */
	@Transactional(readOnly = true)
	public List<TodayClass> today(Student student, UUID myMembershipId) {
		LocalDateTime now = LocalDateTime.now(clock);
		Set<UUID> enrolled = Set.copyOf(enrollments.findClassIds(student.getId()));
		Map<UUID, String> names = staff.namesById(student.getAcademyId());
		return classes.findActive(student.getAcademyId()).stream()
				.filter(c -> c.occursOn(now.toLocalDate()))
				.map(c -> todayView(c, student, enrolled.contains(c.getId()), myMembershipId, names, now))
				.sorted((a, b) -> Boolean.compare(b.enrolled(), a.enrolled()))
				.toList();
	}

	@Transactional
	public void checkIn(Student student, UUID classId, UUID myMembershipId) {
		requireCanTrain(student);
		TrainingClass c = requireOpenNow(student, classId);
		LocalDate today = LocalDate.now(clock);
		sessions.insertIfAbsent(student.getAcademyId(), c.getId(), today);
		ClassSession session = sessions.findByClassIdAndSessionDate(c.getId(), today).orElseThrow();
		checkIns.insertIfAbsent(session.getId(), student.getId(), myMembershipId);
	}

	@Transactional
	public void undo(Student student, UUID classId, UUID myMembershipId) {
		TrainingClass c = requireOpenNow(student, classId);
		sessions.findByClassIdAndSessionDate(c.getId(), LocalDate.now(clock))
				.flatMap(s -> checkIns.findBySessionIdAndStudentId(s.getId(), student.getId()))
				.ifPresent(checkIn -> {
					if (!myMembershipId.equals(checkIn.getMarkedBy())) {
						throw ApiException.forbidden("CHECK_IN_BY_STAFF", "Esta presença foi marcada pelo professor");
					}
					checkIns.delete(checkIn);
				});
	}

	private TodayClass todayView(TrainingClass c, Student student, boolean enrolled, UUID me,
			Map<UUID, String> names, LocalDateTime now) {
		LocalDateTime opens = opensAt(c, now.toLocalDate());
		LocalDateTime closes = closesAt(c, now.toLocalDate());
		var checkIn = sessions.findByClassIdAndSessionDate(c.getId(), now.toLocalDate())
				.flatMap(s -> checkIns.findBySessionIdAndStudentId(s.getId(), student.getId()));
		boolean open = !now.isBefore(opens) && !now.isAfter(closes);
		return new TodayClass(c.getId(), c.getTitle(), c.getStartTime(), c.getEndTime(), c.getLocation(),
				ClassService.instructorOf(c, names), enrolled, checkIn.isPresent(),
				checkIn.map(ci -> me.equals(ci.getMarkedBy())).orElse(false), opens.toLocalTime(),
				closes.toLocalTime(), open);
	}

	private TrainingClass requireOpenNow(Student student, UUID classId) {
		LocalDateTime now = LocalDateTime.now(clock);
		TrainingClass c = classes.findActiveInAcademy(classId, student.getAcademyId())
				.filter(found -> found.occursOn(now.toLocalDate()))
				.orElseThrow(() -> ApiException.notFound("CLASS_NOT_TODAY", "Esta aula não acontece hoje"));
		if (now.isBefore(opensAt(c, now.toLocalDate())) || now.isAfter(closesAt(c, now.toLocalDate()))) {
			throw ApiException.badRequest("CHECK_IN_CLOSED", "O check-in abre 1 hora antes da aula e fecha no fim dela");
		}
		return c;
	}

	private static void requireCanTrain(Student student) {
		student.requireTraining();
		if (student.getStatus() != Status.ACTIVE) {
			throw ApiException.badRequest("CHECK_IN_INACTIVE", "Matrícula inativa: fale com a academia");
		}
	}

	private static LocalDateTime opensAt(TrainingClass c, LocalDate date) {
		return date.atTime(c.getStartTime()).minus(OPENS_BEFORE);
	}

	private static LocalDateTime closesAt(TrainingClass c, LocalDate date) {
		return c.getEndTime() != null ? date.atTime(c.getEndTime()) : date.atTime(c.getStartTime()).plus(DEFAULT_LENGTH);
	}
}
