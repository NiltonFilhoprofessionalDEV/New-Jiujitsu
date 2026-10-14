package br.com.academia.api.team;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import br.com.academia.api.academy.Academy;
import br.com.academia.api.academy.AcademyRepository;
import br.com.academia.api.auth.AuthDtos.IssuedSession;
import br.com.academia.api.auth.AuthService;
import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.common.RandomTokens;
import br.com.academia.api.common.TextNormalizer;
import br.com.academia.api.invite.SignupRateLimiter;
import br.com.academia.api.membership.Membership;
import br.com.academia.api.membership.MembershipRepository;
import br.com.academia.api.membership.MembershipRole;
import br.com.academia.api.membership.StaffService;
import br.com.academia.api.team.StaffInvite.Status;
import br.com.academia.api.team.TeamDtos.CreateStaffInviteRequest;
import br.com.academia.api.team.TeamDtos.PublicStaffInviteInfo;
import br.com.academia.api.team.TeamDtos.StaffInviteView;
import br.com.academia.api.team.TeamDtos.StaffSignupRequest;
import br.com.academia.api.user.User;
import br.com.academia.api.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Convites de equipe: o gestor gera o link; quem recebe cria a conta ou aceita com a conta que já tem. */
@Service
public class StaffInviteService {

	static final Duration INVITE_TTL = Duration.ofDays(7);

	private final StaffInviteRepository invites;
	private final MembershipRepository memberships;
	private final AcademyRepository academies;
	private final UserRepository users;
	private final AuthService auth;
	private final SignupRateLimiter limiter;
	private final Clock clock;

	public StaffInviteService(StaffInviteRepository invites, MembershipRepository memberships,
			AcademyRepository academies, UserRepository users, AuthService auth, SignupRateLimiter limiter,
			Clock clock) {
		this.invites = invites;
		this.memberships = memberships;
		this.academies = academies;
		this.users = users;
		this.auth = auth;
		this.limiter = limiter;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public List<StaffInviteView> pending(UUID academyId) {
		return invites.findByAcademyIdAndStatusAndExpiresAtAfterOrderByCreatedAtDesc(academyId, Status.PENDING,
				clock.instant()).stream().map(StaffInviteView::of).toList();
	}

	/** Só o dono convida co-gestores. */
	@Transactional
	public StaffInviteView create(CurrentUser me, CreateStaffInviteRequest request) {
		TeamService.requireInvitable(request.role());
		if (request.role() == MembershipRole.CO_MANAGER) {
			TeamService.requireOwner(me);
		}
		String email = TextNormalizer.blankToNull(request.email());
		StaffInvite invite = invites.save(new StaffInvite(me.academyId(), RandomTokens.urlSafe(), request.role(),
				TextNormalizer.blankToNull(request.name()), email == null ? null : email.toLowerCase(Locale.ROOT),
				me.userId(), clock.instant().plus(INVITE_TTL)));
		return StaffInviteView.of(invite);
	}

	@Transactional
	public void revoke(UUID academyId, UUID id) {
		StaffInvite invite = invites.findByIdAndAcademyId(id, academyId)
				.orElseThrow(StaffInviteService::notFound);
		requirePending(invite);
		invite.revoke();
	}

	@Transactional(readOnly = true)
	public PublicStaffInviteInfo publicInfo(String token) {
		StaffInvite invite = invites.findByToken(token).orElseThrow(StaffInviteService::notFound);
		return new PublicStaffInviteInfo(academy(invite).getName(), invite.getRole(), invite.getName(),
				invite.getEmail(), invite.view(clock.instant()));
	}

	@Transactional
	public IssuedSession acceptWithNewAccount(String token, StaffSignupRequest request, String clientIp) {
		limiter.consume(clientIp);
		StaffInvite invite = requirePendingForUpdate(token);
		String email = invite.getEmail() != null ? invite.getEmail() : request.email();
		User user = auth.createUser(request.name(), email, request.password());
		return join(invite, user);
	}

	/** Quem já tem conta (ex.: professor em outra academia ou aluno desta) aceita logado. */
	@Transactional
	public IssuedSession acceptWithExistingAccount(String token, UUID userId) {
		StaffInvite invite = requirePendingForUpdate(token);
		User user = users.findById(userId)
				.orElseThrow(() -> ApiException.unauthorized("UNAUTHENTICATED", "Sessão inválida"));
		if (invite.getEmail() != null && !invite.getEmail().equalsIgnoreCase(user.getEmail())) {
			throw ApiException.forbidden("INVITE_EMAIL_MISMATCH", "Este convite foi enviado para outro e-mail");
		}
		if (memberships.existsInAcademyWithRoles(user.getId(), invite.getAcademyId(), StaffService.STAFF_ROLES)) {
			throw ApiException.conflict("ALREADY_STAFF", "Esta conta já faz parte da equipe da academia");
		}
		return join(invite, user);
	}

	private IssuedSession join(StaffInvite invite, User user) {
		Membership membership = memberships.save(new Membership(user, academy(invite), invite.getRole()));
		invite.accept(membership.getId());
		return auth.startSession(membership);
	}

	private StaffInvite requirePendingForUpdate(String token) {
		StaffInvite invite = invites.findForUpdateByToken(token).orElseThrow(StaffInviteService::notFound);
		requirePending(invite);
		return invite;
	}

	private void requirePending(StaffInvite invite) {
		if (invite.view(clock.instant()) != StaffInvite.View.PENDING) {
			throw ApiException.conflict("INVITE_NOT_AVAILABLE", "Este convite não está mais disponível");
		}
	}

	private Academy academy(StaffInvite invite) {
		return academies.findById(invite.getAcademyId())
				.orElseThrow(() -> ApiException.notFound("ACADEMY_NOT_FOUND", "Academia não encontrada"));
	}

	private static ApiException notFound() {
		return ApiException.notFound("INVITE_NOT_FOUND", "Convite não encontrado");
	}
}
