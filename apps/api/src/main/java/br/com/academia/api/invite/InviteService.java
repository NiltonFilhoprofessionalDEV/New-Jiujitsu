package br.com.academia.api.invite;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.RandomTokens;
import br.com.academia.api.common.TextNormalizer;
import br.com.academia.api.invite.Invite.Status;
import br.com.academia.api.invite.InviteDtos.CreateInviteRequest;
import br.com.academia.api.invite.InviteDtos.InviteView;
import br.com.academia.api.invite.InviteDtos.LinkSettings;
import br.com.academia.api.invite.InviteDtos.LinkView;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lado do gestor: link de matrícula da academia e convites individuais. */
@Service
public class InviteService {

	static final Duration INVITE_TTL = Duration.ofDays(7);
	private static final Limit LIST_LIMIT = Limit.of(200);

	enum Tab {
		PENDING, ACCEPTED, EXPIRED, ALL
	}

	private final EnrollmentLinkRepository links;
	private final InviteRepository invites;
	private final Clock clock;

	public InviteService(EnrollmentLinkRepository links, InviteRepository invites, Clock clock) {
		this.links = links;
		this.invites = invites;
		this.clock = clock;
	}

	/** O link nasce no primeiro acesso do gestor. */
	@Transactional
	public LinkView link(UUID academyId) {
		return view(linkFor(academyId));
	}

	@Transactional
	public LinkView rotateLink(UUID academyId) {
		EnrollmentLink link = linkFor(academyId);
		link.rotate(RandomTokens.urlSafe());
		return view(link);
	}

	@Transactional
	public LinkView configureLink(UUID academyId, LinkSettings settings) {
		EnrollmentLink link = linkFor(academyId);
		link.configure(settings.enabled(), settings.expiresOn());
		return view(link);
	}

	@Transactional(readOnly = true)
	public List<InviteView> list(UUID academyId, String rawTab) {
		Instant now = clock.instant();
		List<Invite> found = switch (parseTab(rawTab)) {
			case PENDING -> invites.findByAcademyIdAndStatusAndExpiresAtAfterOrderByCreatedAtDesc(
					academyId, Status.PENDING, now, LIST_LIMIT);
			case EXPIRED -> invites.findByAcademyIdAndStatusAndExpiresAtLessThanEqualOrderByCreatedAtDesc(
					academyId, Status.PENDING, now, LIST_LIMIT);
			case ACCEPTED -> invites.findByAcademyIdAndStatusOrderByCreatedAtDesc(academyId, Status.ACCEPTED, LIST_LIMIT);
			case ALL -> invites.findByAcademyIdOrderByCreatedAtDesc(academyId, LIST_LIMIT);
		};
		return found.stream().map(i -> InviteView.of(i, now)).toList();
	}

	@Transactional
	public InviteView create(UUID academyId, CreateInviteRequest request, UUID createdBy) {
		String email = TextNormalizer.blankToNull(request.email());
		CreateInviteRequest normalized = new CreateInviteRequest(
				request.kind(),
				TextNormalizer.blankToNull(request.name()),
				email == null ? null : email.toLowerCase(Locale.ROOT),
				TextNormalizer.blankToNull(request.phone()));
		Instant now = clock.instant();
		Invite invite = invites.save(new Invite(academyId, RandomTokens.urlSafe(), normalized, createdBy,
				now.plus(INVITE_TTL)));
		return InviteView.of(invite, now);
	}

	@Transactional
	public InviteView revoke(UUID academyId, UUID id) {
		Invite invite = requireNotDecided(academyId, id);
		invite.revoke();
		return InviteView.of(invite, clock.instant());
	}

	/** Renova a validade por mais {@link #INVITE_TTL}, inclusive de convite já expirado. */
	@Transactional
	public InviteView renew(UUID academyId, UUID id) {
		Invite invite = requireNotDecided(academyId, id);
		Instant now = clock.instant();
		invite.renew(now.plus(INVITE_TTL));
		return InviteView.of(invite, now);
	}

	private Invite requireNotDecided(UUID academyId, UUID id) {
		Invite invite = invites.findByIdAndAcademyId(id, academyId)
				.orElseThrow(() -> ApiException.notFound("INVITE_NOT_FOUND", "Convite não encontrado"));
		Invite.View view = invite.view(clock.instant());
		if (view != Invite.View.PENDING && view != Invite.View.EXPIRED) {
			throw ApiException.conflict("INVITE_NOT_AVAILABLE", "Este convite não está mais disponível");
		}
		return invite;
	}

	private EnrollmentLink linkFor(UUID academyId) {
		return links.findById(academyId)
				.orElseGet(() -> links.save(new EnrollmentLink(academyId, RandomTokens.urlSafe())));
	}

	private LinkView view(EnrollmentLink link) {
		return new LinkView(link.getToken(), link.isEnabled(), link.getExpiresOn(), link.getOpens(),
				link.state(LocalDate.now(clock)));
	}

	private static Tab parseTab(String raw) {
		if (raw == null || raw.isBlank()) {
			return Tab.PENDING;
		}
		return Arrays.stream(Tab.values())
				.filter(t -> t.name().equalsIgnoreCase(raw))
				.findFirst()
				.orElseThrow(() -> ApiException.badRequest("INVALID_FILTER", "Filtro inválido"));
	}
}
