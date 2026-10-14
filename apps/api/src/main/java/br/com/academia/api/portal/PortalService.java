package br.com.academia.api.portal;

import java.util.List;
import java.util.UUID;

import br.com.academia.api.billing.InvoiceService;
import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.graduation.GraduationDtos.StudentGraduations;
import br.com.academia.api.graduation.GraduationService;
import br.com.academia.api.portal.PortalDtos.BeltProgress;
import br.com.academia.api.portal.PortalDtos.GraduationItem;
import br.com.academia.api.portal.PortalDtos.InvoiceItem;
import br.com.academia.api.portal.PortalDtos.Overview;
import br.com.academia.api.portal.PortalDtos.Profile;
import br.com.academia.api.student.Student;
import br.com.academia.api.student.StudentEnums.Kind;
import br.com.academia.api.training.ScheduleDtos.TodayClass;
import br.com.academia.api.training.StudentScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortalService {

	static final int RECENT_ATTENDANCE = 20;
	static final int INVOICE_LIMIT = 24;

	private final PortalAccess access;
	private final StudentScheduleService schedule;
	private final GraduationService graduations;
	private final InvoiceService invoices;

	public PortalService(PortalAccess access, StudentScheduleService schedule, GraduationService graduations,
			InvoiceService invoices) {
		this.access = access;
		this.schedule = schedule;
		this.graduations = graduations;
		this.invoices = invoices;
	}

	@Transactional(readOnly = true)
	public List<Profile> profiles(CurrentUser me) {
		return access.profiles(me).stream().map(s -> Profile.of(s, PortalAccess.isSelf(s, me))).toList();
	}

	@Transactional(readOnly = true)
	public Overview overview(CurrentUser me, UUID studentId) {
		Student student = access.require(me, studentId);
		Profile profile = Profile.of(student, PortalAccess.isSelf(student, me));
		if (student.getKind() != Kind.TRAINING) {
			return new Overview(profile, List.of(), 0, List.of(), List.of(), null);
		}
		StudentGraduations history = graduations.history(me.academyId(), studentId);
		return new Overview(profile,
				schedule.enrolledClasses(student),
				schedule.attendanceCount(student),
				schedule.recentAttendance(student, RECENT_ATTENDANCE),
				history.history().stream().map(GraduationItem::of).toList(),
				history.progress() == null ? null : BeltProgress.of(history.progress()));
	}

	@Transactional(readOnly = true)
	public List<InvoiceItem> invoices(CurrentUser me, UUID studentId) {
		access.require(me, studentId);
		return invoices.forStudent(me.academyId(), studentId, INVOICE_LIMIT).stream().map(InvoiceItem::of).toList();
	}

	@Transactional(readOnly = true)
	public List<TodayClass> today(CurrentUser me, UUID studentId) {
		Student student = access.require(me, studentId);
		return student.getKind() == Kind.TRAINING ? schedule.today(student, me.membershipId()) : List.of();
	}

	@Transactional
	public List<TodayClass> checkIn(CurrentUser me, UUID studentId, UUID classId) {
		Student student = access.require(me, studentId);
		schedule.checkIn(student, classId, me.membershipId());
		return schedule.today(student, me.membershipId());
	}

	@Transactional
	public List<TodayClass> undoCheckIn(CurrentUser me, UUID studentId, UUID classId) {
		Student student = access.require(me, studentId);
		schedule.undo(student, classId, me.membershipId());
		return schedule.today(student, me.membershipId());
	}
}
