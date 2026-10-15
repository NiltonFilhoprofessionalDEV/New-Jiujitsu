package br.com.academia.api.access;

import br.com.academia.api.access.AcademyAccessDtos.AccessView;
import br.com.academia.api.common.CurrentUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Assinatura da academia na plataforma (não confundir com as mensalidades dos alunos). */
@RestController
@RequestMapping("/academy-access")
public class AcademyAccessController {

	private final AcademyAccessService access;

	public AcademyAccessController(AcademyAccessService access) {
		this.access = access;
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('OWNER','CO_MANAGER','INSTRUCTOR')")
	public AccessView current() {
		return access.view(CurrentUser.get().academyId());
	}
}
