package br.com.academia.api.invite;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.invite.EnrollmentRequest.Status;
import br.com.academia.api.invite.InviteDtos.EnrollmentRequestView;
import br.com.academia.api.invite.InviteDtos.PendingCount;
import br.com.academia.api.membership.Membership;
import br.com.academia.api.membership.MembershipRepository;
import br.com.academia.api.student.StudentDtos.StudentDetail;
import br.com.academia.api.student.StudentEnums.Kind;
import br.com.academia.api.student.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Solicitações de cadastro vindas do link de matrícula. */
@Service
public class EnrollmentRequestService {

	private final EnrollmentRequestRepository requests;
	private final MembershipRepository memberships;
	private final StudentService students;
	private final Clock clock;

	public EnrollmentRequestService(EnrollmentRequestRepository requests, MembershipRepository memberships,
			StudentService students, Clock clock) {
		this.requests = requests;
		this.memberships = memberships;
		this.students = students;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public List<EnrollmentRequestView> pending(UUID academyId) {
		return requests.findByAcademyIdAndStatusOrderByCreatedAtAsc(academyId, Status.PENDING).stream()
				.map(EnrollmentRequestView::of)
				.toList();
	}

	@Transactional(readOnly = true)
	public PendingCount count(UUID academyId) {
		return new PendingCount(requests.countByAcademyIdAndStatus(academyId, Status.PENDING));
	}

	/** Cria o aluno ligado à conta e libera o login. */
	@Transactional
	public StudentDetail approve(UUID academyId, UUID id, UUID decidedBy) {
		EnrollmentRequest request = requirePending(academyId, id);
		Membership membership = memberships.findById(request.getMembershipId())
				.orElseThrow(() -> ApiException.conflict("REQUEST_ALREADY_DECIDED", "Esta solicitação já foi resolvida"));
		StudentDetail student = students.create(academyId, request.toSignupData().toStudentRequest(Kind.TRAINING),
				null, request.getUserId());
		membership.activate();
		request.approve(student.id(), decidedBy, clock.instant());
		return student;
	}

	/** A conta continua existindo, mas sem acesso a esta academia. */
	@Transactional
	public void reject(UUID academyId, UUID id, UUID decidedBy) {
		requirePending(academyId, id).reject(decidedBy, clock.instant());
	}

	private EnrollmentRequest requirePending(UUID academyId, UUID id) {
		EnrollmentRequest request = requests.findForUpdate(id, academyId)
				.orElseThrow(() -> ApiException.notFound("REQUEST_NOT_FOUND", "Solicitação não encontrada"));
		if (request.getStatus() != Status.PENDING) {
			throw ApiException.conflict("REQUEST_ALREADY_DECIDED", "Esta solicitação já foi resolvida");
		}
		return request;
	}
}
