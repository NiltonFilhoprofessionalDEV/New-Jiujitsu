package br.com.academia.api.report;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import br.com.academia.api.billing.InvoiceService;
import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.membership.StaffService;
import br.com.academia.api.membership.StaffService.StaffMember;
import br.com.academia.api.report.ReportDtos.ClassRow;
import br.com.academia.api.report.ReportDtos.InstructorRow;
import br.com.academia.api.report.ReportDtos.Period;
import br.com.academia.api.report.ReportDtos.Report;
import br.com.academia.api.report.ReportQuery.InstructorCounts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

	static final int DEFAULT_DAYS = 30;
	static final int MAX_DAYS = 366;
	static final int ABSENT_AFTER_DAYS = 14;
	static final int TOP_STUDENTS = 10;
	static final int ABSENT_LIMIT = 20;

	private static final InstructorCounts NO_CLASSES = new InstructorCounts(null, 0, 0, 0);

	private final ReportQuery query;
	private final StaffService staff;
	private final InvoiceService invoices;
	private final Clock clock;

	public ReportService(ReportQuery query, StaffService staff, InvoiceService invoices, Clock clock) {
		this.query = query;
		this.staff = staff;
		this.invoices = invoices;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public Report build(CurrentUser user, LocalDate from, LocalDate to) {
		UUID academyId = user.academyId();
		LocalDate today = LocalDate.now(clock);
		Period period = period(from, to, today);
		List<StaffMember> team = staff.list(academyId);
		Map<UUID, String> names = team.stream().collect(Collectors.toMap(StaffMember::membershipId, StaffMember::name));
		List<ClassRow> classes = query.classes(academyId, period).stream()
				.map(c -> new ClassRow(c.classId(), c.title(), names.get(c.instructorId()), c.sessions(), c.checkIns()))
				.toList();
		return new Report(period,
				query.students(academyId, period),
				query.attendance(academyId, period),
				classes,
				instructors(team, query.instructors(academyId, period)),
				query.topStudents(academyId, period, TOP_STUDENTS),
				query.absent(academyId, today.minusDays(ABSENT_AFTER_DAYS), ABSENT_LIMIT),
				user.isManager() ? invoices.periodSummary(academyId, period.from(), period.to()) : null);
	}

	/** Sem datas: os últimos {@value #DEFAULT_DAYS} dias até hoje. */
	static Period period(LocalDate from, LocalDate to, LocalDate today) {
		LocalDate end = to == null ? today : to;
		LocalDate start = from == null ? end.minusDays(DEFAULT_DAYS - 1L) : from;
		if (start.isAfter(end)) {
			throw ApiException.badRequest("INVALID_PERIOD", "A data inicial precisa ser anterior à final");
		}
		if (ChronoUnit.DAYS.between(start, end) >= MAX_DAYS) {
			throw ApiException.badRequest("INVALID_PERIOD", "O período pode ter no máximo " + MAX_DAYS + " dias");
		}
		return new Period(start, end);
	}

	/** Toda a equipe atual, inclusive quem não tem turma; mais aulas dadas primeiro. */
	private static List<InstructorRow> instructors(List<StaffMember> team, List<InstructorCounts> counts) {
		Map<UUID, InstructorCounts> byId = counts.stream()
				.collect(Collectors.toMap(InstructorCounts::membershipId, Function.identity()));
		return team.stream()
				.map(m -> {
					InstructorCounts c = byId.getOrDefault(m.membershipId(), NO_CLASSES);
					return new InstructorRow(m.membershipId(), m.name(), c.classes(), c.sessions(), c.checkIns());
				})
				.sorted((a, b) -> Long.compare(b.sessions(), a.sessions()))
				.toList();
	}
}
