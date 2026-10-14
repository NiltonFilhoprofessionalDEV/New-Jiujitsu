package br.com.academia.api.graduation;

import java.util.UUID;

import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.graduation.GraduationDtos.GraduationRequest;
import br.com.academia.api.graduation.GraduationDtos.StudentGraduations;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Histórico na ficha do aluno: a equipe consulta; só gestores registram ou removem. */
@RestController
@RequestMapping("/students/{studentId}/graduations")
@PreAuthorize("hasAnyRole('OWNER','CO_MANAGER','INSTRUCTOR')")
public class GraduationController {

	private static final String MANAGERS = "hasAnyRole('OWNER','CO_MANAGER')";

	private final GraduationService service;

	public GraduationController(GraduationService service) {
		this.service = service;
	}

	@GetMapping
	public StudentGraduations history(@PathVariable UUID studentId) {
		return service.history(CurrentUser.get().academyId(), studentId);
	}

	@PostMapping
	@PreAuthorize(MANAGERS)
	@ResponseStatus(HttpStatus.CREATED)
	public StudentGraduations register(@PathVariable UUID studentId, @Valid @RequestBody GraduationRequest body) {
		CurrentUser user = CurrentUser.get();
		return service.register(user.academyId(), user.membershipId(), studentId, body);
	}

	@DeleteMapping("/{graduationId}")
	@PreAuthorize(MANAGERS)
	public StudentGraduations remove(@PathVariable UUID studentId, @PathVariable UUID graduationId) {
		return service.removeLatest(CurrentUser.get().academyId(), studentId, graduationId);
	}
}
