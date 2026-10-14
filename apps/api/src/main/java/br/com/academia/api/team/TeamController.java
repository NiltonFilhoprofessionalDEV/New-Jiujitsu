package br.com.academia.api.team;

import java.util.List;
import java.util.UUID;

import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.team.TeamDtos.CreateStaffInviteRequest;
import br.com.academia.api.team.TeamDtos.RoleRequest;
import br.com.academia.api.team.TeamDtos.StaffInviteView;
import br.com.academia.api.team.TeamDtos.TeamMember;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/team")
@PreAuthorize("hasAnyRole('OWNER','CO_MANAGER')")
public class TeamController {

	private final TeamService team;
	private final StaffInviteService invites;

	public TeamController(TeamService team, StaffInviteService invites) {
		this.team = team;
		this.invites = invites;
	}

	@GetMapping
	public List<TeamMember> members() {
		return team.list(CurrentUser.get());
	}

	@PutMapping("/{membershipId}/role")
	public TeamMember changeRole(@PathVariable UUID membershipId, @Valid @RequestBody RoleRequest body) {
		return team.changeRole(CurrentUser.get(), membershipId, body.role());
	}

	@DeleteMapping("/{membershipId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void remove(@PathVariable UUID membershipId) {
		team.remove(CurrentUser.get(), membershipId);
	}

	@GetMapping("/invites")
	public List<StaffInviteView> pendingInvites() {
		return invites.pending(CurrentUser.get().academyId());
	}

	@PostMapping("/invites")
	@ResponseStatus(HttpStatus.CREATED)
	public StaffInviteView invite(@Valid @RequestBody CreateStaffInviteRequest body) {
		return invites.create(CurrentUser.get(), body);
	}

	@DeleteMapping("/invites/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void revokeInvite(@PathVariable UUID id) {
		invites.revoke(CurrentUser.get().academyId(), id);
	}
}
