package br.com.academia.api.membership;

import java.util.List;

import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.membership.StaffService.StaffMember;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/staff")
@PreAuthorize("hasAnyRole('OWNER','CO_MANAGER','INSTRUCTOR')")
public class StaffController {

	private final StaffService service;

	public StaffController(StaffService service) {
		this.service = service;
	}

	@GetMapping
	public List<StaffMember> list() {
		return service.list(CurrentUser.get().academyId());
	}
}
