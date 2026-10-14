package br.com.academia.api.student;

import java.util.UUID;

import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.common.PageResponse;
import br.com.academia.api.student.StudentDtos.StudentCounts;
import br.com.academia.api.student.StudentDtos.StudentDetail;
import br.com.academia.api.student.StudentDtos.StudentListItem;
import br.com.academia.api.student.StudentDtos.StudentRequest;
import br.com.academia.api.student.StudentEnums.Status;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/students")
@PreAuthorize("hasAnyRole('OWNER','CO_MANAGER','INSTRUCTOR')")
public class StudentController {

	private static final String MANAGERS = "hasAnyRole('OWNER','CO_MANAGER')";

	private final StudentService service;

	public StudentController(StudentService service) {
		this.service = service;
	}

	@GetMapping("/page")
	public PageResponse<StudentListItem> page(
			@RequestParam(required = false) String q,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String kind,
			@RequestParam(required = false) String age,
			@RequestParam(required = false) String household,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		StudentFilter filter = StudentFilter.of(q, status, kind, age, household);
		return PageResponse.of(service.page(academyId(), filter, PageRequest.of(page, size)));
	}

	@GetMapping("/counts")
	public StudentCounts counts(
			@RequestParam(required = false) String q,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String age) {
		return service.counts(academyId(), StudentFilter.of(q, status, null, age, null));
	}

	@GetMapping("/{id}")
	public StudentDetail get(@PathVariable UUID id) {
		return service.get(academyId(), id);
	}

	@PostMapping
	@PreAuthorize(MANAGERS)
	@ResponseStatus(HttpStatus.CREATED)
	public StudentDetail create(@Valid @RequestBody StudentRequest body) {
		return service.create(academyId(), body, null);
	}

	@PutMapping("/{id}")
	@PreAuthorize(MANAGERS)
	public StudentDetail update(@PathVariable UUID id, @Valid @RequestBody StudentRequest body) {
		return service.update(academyId(), id, body);
	}

	@PostMapping("/{id}/deactivate")
	@PreAuthorize(MANAGERS)
	public StudentDetail deactivate(@PathVariable UUID id) {
		return service.setStatus(academyId(), id, Status.INACTIVE);
	}

	@PostMapping("/{id}/reactivate")
	@PreAuthorize(MANAGERS)
	public StudentDetail reactivate(@PathVariable UUID id) {
		return service.setStatus(academyId(), id, Status.ACTIVE);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize(MANAGERS)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		service.delete(academyId(), id);
	}

	private static UUID academyId() {
		return CurrentUser.get().academyId();
	}
}
