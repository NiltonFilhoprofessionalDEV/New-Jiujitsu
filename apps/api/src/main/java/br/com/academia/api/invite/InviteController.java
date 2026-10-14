package br.com.academia.api.invite;

import java.util.List;
import java.util.UUID;

import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.invite.InviteDtos.CreateInviteRequest;
import br.com.academia.api.invite.InviteDtos.InviteView;
import br.com.academia.api.invite.InviteDtos.LinkSettings;
import br.com.academia.api.invite.InviteDtos.LinkView;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/invites")
@PreAuthorize("hasAnyRole('OWNER','CO_MANAGER')")
public class InviteController {

	private final InviteService service;

	public InviteController(InviteService service) {
		this.service = service;
	}

	@GetMapping("/link")
	public LinkView link() {
		return service.link(academyId());
	}

	@PostMapping("/link/rotate")
	public LinkView rotateLink() {
		return service.rotateLink(academyId());
	}

	@PutMapping("/link")
	public LinkView configureLink(@Valid @RequestBody LinkSettings body) {
		return service.configureLink(academyId(), body);
	}

	@GetMapping
	public List<InviteView> list(@RequestParam(required = false) String status) {
		return service.list(academyId(), status);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public InviteView create(@Valid @RequestBody CreateInviteRequest body) {
		CurrentUser me = CurrentUser.get();
		return service.create(me.academyId(), body, me.userId());
	}

	@PostMapping("/{id}/revoke")
	public InviteView revoke(@PathVariable UUID id) {
		return service.revoke(academyId(), id);
	}

	@PostMapping("/{id}/renew")
	public InviteView renew(@PathVariable UUID id) {
		return service.renew(academyId(), id);
	}

	private static UUID academyId() {
		return CurrentUser.get().academyId();
	}
}
