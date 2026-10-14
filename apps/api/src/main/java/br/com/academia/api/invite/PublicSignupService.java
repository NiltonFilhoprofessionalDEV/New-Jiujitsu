package br.com.academia.api.invite;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

import br.com.academia.api.academy.Academy;
import br.com.academia.api.academy.AcademyRepository;
import br.com.academia.api.auth.AuthDtos.IssuedSession;
import br.com.academia.api.auth.AuthService;
import br.com.academia.api.common.ApiException;
import br.com.academia.api.invite.EnrollmentLink.State;
import br.com.academia.api.invite.InviteDtos.PublicInviteInfo;
import br.com.academia.api.invite.InviteDtos.PublicLinkInfo;
import br.com.academia.api.invite.InviteDtos.SignupRequest;
import br.com.academia.api.membership.Membership;
import br.com.academia.api.membership.MembershipRepository;
import br.com.academia.api.membership.MembershipRole;
import br.com.academia.api.student.StudentDtos.StudentDetail;
import br.com.academia.api.student.StudentEnums.Kind;
import br.com.academia.api.student.StudentService;
import br.com.academia.api.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lado público, sem login: quem recebeu o link de matrícula ou um convite. */
@Service
public class PublicSignupService {

	private final EnrollmentLinkRepository links;
	private final InviteRepository invites;
	private final EnrollmentRequestRepository requests;
	private final AcademyRepository academies;
	private final MembershipRepository memberships;
	private final AuthService auth;
	private final StudentService students;
	private final SignupRateLimiter limiter;
	private final Clock clock;

	public PublicSignupService(EnrollmentLinkRepository links, InviteRepository invites,
			EnrollmentRequestRepository requests, AcademyRepository academies, MembershipRepository memberships,
			AuthService auth, StudentService students, SignupRateLimiter limiter, Clock clock) {
		this.links = links;
		this.invites = invites;
		this.requests = requests;
		this.academies = academies;
		this.memberships = memberships;
		this.auth = auth;
		this.students = students;
		this.limiter = limiter;
		this.clock = clock;
	}

	@Transactional
	public PublicLinkInfo linkInfo(String token) {
		EnrollmentLink link = requireLink(token);
		link.registerOpen();
		return new PublicLinkInfo(academy(link.getAcademyId()).getName(), link.state(LocalDate.now(clock)));
	}

	/** Cria a conta com vínculo inativo e uma solicitação; o gestor aprova em Solicitações. */
	@Transactional
	public void enroll(String token, SignupRequest request, String clientIp) {
		limiter.consume(clientIp);
		EnrollmentLink link = requireLink(token);
		if (link.state(LocalDate.now(clock)) != State.OPEN) {
			throw ApiException.conflict("ENROLLMENT_CLOSED", "Este link não está aceitando cadastros");
		}
		UUID academyId = link.getAcademyId();
		SignupData data = SignupData.of(request, Kind.TRAINING, null);
		students.ensureAvailable(academyId, data.email(), data.cpf());

		User user = auth.createUser(data.name(), data.email(), request.password());
		Membership membership = memberships.save(Membership.pending(user, academy(academyId), MembershipRole.STUDENT));
		requests.save(new EnrollmentRequest(academyId, user.getId(), membership.getId(), data));
	}

	@Transactional
	public PublicInviteInfo inviteInfo(String token) {
		Invite invite = invites.findByToken(token).orElseThrow(PublicSignupService::inviteNotFound);
		invite.registerOpen();
		return new PublicInviteInfo(academy(invite.getAcademyId()).getName(), invite.getKind(), invite.getName(),
				invite.getEmail(), invite.view(clock.instant()));
	}

	/** Convite é decisão do gestor: o aceite já cria o aluno ativo e abre a sessão. */
	@Transactional
	public IssuedSession acceptInvite(String token, SignupRequest request, String clientIp) {
		limiter.consume(clientIp);
		Invite invite = invites.findForUpdateByToken(token).orElseThrow(PublicSignupService::inviteNotFound);
		if (invite.view(clock.instant()) != Invite.View.PENDING) {
			throw ApiException.conflict("INVITE_NOT_AVAILABLE", "Este convite não está mais disponível");
		}
		UUID academyId = invite.getAcademyId();
		Kind kind = invite.getKind();
		SignupData data = SignupData.of(request, kind, invite.getEmail());
		students.ensureAvailable(academyId, data.email(), data.cpf());

		User user = auth.createUser(data.name(), data.email(), request.password());
		MembershipRole role = kind == Kind.TRAINING ? MembershipRole.STUDENT : MembershipRole.RESPONSIBLE;
		Membership membership = memberships.save(new Membership(user, academy(academyId), role));
		StudentDetail student = students.create(academyId, data.toStudentRequest(kind), null, user.getId());
		invite.accept(student.id());
		return auth.startSession(membership);
	}

	private EnrollmentLink requireLink(String token) {
		return links.findByToken(token)
				.orElseThrow(() -> ApiException.notFound("LINK_NOT_FOUND", "Link de matrícula inválido"));
	}

	private Academy academy(UUID academyId) {
		return academies.findById(academyId)
				.orElseThrow(() -> ApiException.notFound("ACADEMY_NOT_FOUND", "Academia não encontrada"));
	}

	private static ApiException inviteNotFound() {
		return ApiException.notFound("INVITE_NOT_FOUND", "Convite não encontrado");
	}
}
