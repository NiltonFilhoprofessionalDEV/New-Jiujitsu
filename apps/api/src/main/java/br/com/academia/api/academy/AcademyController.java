package br.com.academia.api.academy;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/academies/current")
public class AcademyController {

	private final AcademyRepository academies;

	public AcademyController(AcademyRepository academies) {
		this.academies = academies;
	}

	@GetMapping("/profile")
	@Transactional(readOnly = true)
	public AcademyProfile profile() {
		return current().toProfile();
	}

	@PutMapping("/profile")
	@PreAuthorize("hasAnyRole('OWNER','CO_MANAGER')")
	@Transactional
	public AcademyProfile updateProfile(@Valid @RequestBody AcademyProfile body) {
		Academy academy = current();
		academy.updateProfile(body);
		return academy.toProfile();
	}

	private Academy current() {
		return academies.findById(CurrentUser.get().academyId())
				.orElseThrow(() -> ApiException.notFound("ACADEMY_NOT_FOUND", "Academia não encontrada"));
	}
}
