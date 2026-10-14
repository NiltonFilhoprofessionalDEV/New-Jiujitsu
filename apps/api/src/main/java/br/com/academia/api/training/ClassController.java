package br.com.academia.api.training;

import java.util.List;
import java.util.UUID;

import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.student.HouseholdDtos.StudentIdRequest;
import br.com.academia.api.training.ClassDtos.ClassRequest;
import br.com.academia.api.training.ClassDtos.ClassStudent;
import br.com.academia.api.training.ClassDtos.ClassView;
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
@RequestMapping("/classes")
@PreAuthorize("hasAnyRole('OWNER','CO_MANAGER','INSTRUCTOR')")
public class ClassController {

	private static final String MANAGERS = "hasAnyRole('OWNER','CO_MANAGER')";

	private final ClassService service;

	public ClassController(ClassService service) {
		this.service = service;
	}

	@GetMapping
	public List<ClassView> list() {
		return service.list(academyId());
	}

	@PostMapping
	@PreAuthorize(MANAGERS)
	@ResponseStatus(HttpStatus.CREATED)
	public ClassView create(@Valid @RequestBody ClassRequest body) {
		return service.create(academyId(), body);
	}

	@PutMapping("/{id}")
	@PreAuthorize(MANAGERS)
	public ClassView update(@PathVariable UUID id, @Valid @RequestBody ClassRequest body) {
		return service.update(academyId(), id, body);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize(MANAGERS)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void archive(@PathVariable UUID id) {
		service.archive(academyId(), id);
	}

	@GetMapping("/{id}/students")
	public List<ClassStudent> students(@PathVariable UUID id) {
		return service.students(academyId(), id);
	}

	@PostMapping("/{id}/students")
	@PreAuthorize(MANAGERS)
	public List<ClassStudent> enroll(@PathVariable UUID id, @Valid @RequestBody StudentIdRequest body) {
		return service.enroll(academyId(), id, body.studentId());
	}

	@DeleteMapping("/{id}/students/{studentId}")
	@PreAuthorize(MANAGERS)
	public List<ClassStudent> unenroll(@PathVariable UUID id, @PathVariable UUID studentId) {
		return service.unenroll(academyId(), id, studentId);
	}

	private static UUID academyId() {
		return CurrentUser.get().academyId();
	}
}
