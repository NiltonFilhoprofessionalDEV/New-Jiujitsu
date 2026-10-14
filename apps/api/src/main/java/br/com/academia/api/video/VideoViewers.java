package br.com.academia.api.video;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.membership.StaffService;
import br.com.academia.api.portal.PortalAccess;
import br.com.academia.api.student.Student;
import br.com.academia.api.student.StudentEnums.Kind;
import br.com.academia.api.student.StudentEnums.Status;
import br.com.academia.api.training.ClassEnrollmentRepository;
import org.springframework.stereotype.Component;

@Component
class VideoViewers {

	private final PortalAccess portal;
	private final ClassEnrollmentRepository enrollments;

	VideoViewers(PortalAccess portal, ClassEnrollmentRepository enrollments) {
		this.portal = portal;
		this.enrollments = enrollments;
	}

	/** Chamar dentro de uma transação: lê os perfis e as inscrições da conta. */
	VideoViewer of(CurrentUser me) {
		if (StaffService.STAFF_ROLES.contains(me.role())) {
			return new VideoViewer(me.academyId(), me.membershipId(), true, me.isManager(), true, Set.of(), Set.of());
		}
		List<Student> active = portal.profiles(me).stream().filter(s -> s.getStatus() == Status.ACTIVE).toList();
		Set<String> belts = new HashSet<>();
		Set<UUID> classIds = new HashSet<>();
		active.stream().filter(s -> s.getKind() == Kind.TRAINING).forEach(s -> {
			if (s.getBelt() != null) {
				belts.add(s.getBelt().slug());
			}
			classIds.addAll(enrollments.findClassIds(s.getId()));
		});
		return new VideoViewer(me.academyId(), me.membershipId(), false, false, !active.isEmpty(), belts, classIds);
	}
}
