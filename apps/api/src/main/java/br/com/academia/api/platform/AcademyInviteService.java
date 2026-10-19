package br.com.academia.api.platform;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import br.com.academia.api.auth.AuthDtos.IssuedSession;
import br.com.academia.api.auth.AuthDtos.RegisterAcademyRequest;
import br.com.academia.api.auth.AuthService;
import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.common.RandomTokens;
import br.com.academia.api.invite.SignupRateLimiter;
import br.com.academia.api.platform.AcademyCreationInvite.Status;
import br.com.academia.api.platform.AcademyInviteDtos.AcademyInviteView;
import br.com.academia.api.platform.AcademyInviteDtos.PublicAcademyInviteInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Convite de academia: só o admin da plataforma gera o link; quem recebe cria a academia. */
@Service
public class AcademyInviteService {

	static final Duration INVITE_TTL = Duration.ofDays(7);

	private final AcademyCreationInviteRepository invites;
	private final PlatformAdmins admins;
	private final AuthService auth;
	private final SignupRateLimiter limiter;
	private final Clock clock;

	public AcademyInviteService(AcademyCreationInviteRepository invites, PlatformAdmins admins, AuthService auth,
			SignupRateLimiter limiter, Clock clock) {
		this.invites = invites;
		this.admins = admins;
		this.auth = auth;
		this.limiter = limiter;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public List<AcademyInviteView> pending(CurrentUser me) {
		admins.requireAdmin(me.userId());
		return invites.findByStatusAndExpiresAtAfterOrderByCreatedAtDesc(Status.PENDING, clock.instant()).stream()
				.map(AcademyInviteView::of).toList();
	}

	@Transactional
	public AcademyInviteView create(CurrentUser me) {
		admins.requireAdmin(me.userId());
		AcademyCreationInvite invite = invites.save(new AcademyCreationInvite(RandomTokens.urlSafe(), me.userId(),
				clock.instant().plus(INVITE_TTL)));
		return AcademyInviteView.of(invite);
	}

	@Transactional
	public void revoke(CurrentUser me, UUID id) {
		admins.requireAdmin(me.userId());
		AcademyCreationInvite invite = invites.findById(id).orElseThrow(AcademyInviteService::notFound);
		requirePending(invite);
		invite.revoke();
	}

	@Transactional(readOnly = true)
	public PublicAcademyInviteInfo publicInfo(String token) {
		AcademyCreationInvite invite = invites.findByToken(token).orElseThrow(AcademyInviteService::notFound);
		return new PublicAcademyInviteInfo(invite.view(clock.instant()));
	}

	@Transactional
	public IssuedSession accept(String token, RegisterAcademyRequest request, String clientIp) {
		limiter.consume(clientIp);
		AcademyCreationInvite invite = requirePendingForUpdate(token);
		IssuedSession session = auth.registerAcademy(request);
		invite.accept(academyIdOf(session));
		return session;
	}

	private AcademyCreationInvite requirePendingForUpdate(String token) {
		AcademyCreationInvite invite = invites.findForUpdateByToken(token).orElseThrow(AcademyInviteService::notFound);
		requirePending(invite);
		return invite;
	}

	private void requirePending(AcademyCreationInvite invite) {
		if (invite.view(clock.instant()) != AcademyCreationInvite.View.PENDING) {
			throw ApiException.conflict("INVITE_NOT_AVAILABLE", "Este convite não está mais disponível");
		}
	}

	private static UUID academyIdOf(IssuedSession session) {
		UUID active = session.body().activeMembershipId();
		return session.body().memberships().stream()
				.filter(membership -> membership.id().equals(active))
				.map(membership -> membership.academy().id())
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Sessão sem academia ativa"));
	}

	private static ApiException notFound() {
		return ApiException.notFound("INVITE_NOT_FOUND", "Convite não encontrado");
	}
}
