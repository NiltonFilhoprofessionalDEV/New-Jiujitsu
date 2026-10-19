package br.com.academia.api.invite;

import java.util.List;
import java.util.UUID;

import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.invite.InviteDtos.ApproveEnrollmentRequest;
import br.com.academia.api.invite.InviteDtos.EnrollmentRequestView;
import br.com.academia.api.invite.InviteDtos.PendingCount;
import br.com.academia.api.student.StudentDtos.StudentDetail;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/requests")
@PreAuthorize("hasAnyRole('OWNER','CO_MANAGER')")
public class EnrollmentRequestController {

	private final EnrollmentRequestService service;

	public EnrollmentRequestController(EnrollmentRequestService service) {
		this.service = service;
	}

	@GetMapping("/enrollments")
	public List<EnrollmentRequestView> pending() {
		return service.pending(CurrentUser.get().academyId());
	}

	@GetMapping("/count")
	public PendingCount count() {
		return service.count(CurrentUser.get().academyId());
	}

	@PostMapping("/enrollments/{id}/approve")
	public StudentDetail approve(@PathVariable UUID id, @Valid @RequestBody ApproveEnrollmentRequest body) {
		CurrentUser me = CurrentUser.get();
		return service.approve(me.academyId(), id, me.userId(), body);
	}

	@PostMapping("/enrollments/{id}/reject")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void reject(@PathVariable UUID id) {
		CurrentUser me = CurrentUser.get();
		service.reject(me.academyId(), id, me.userId());
	}
}
