package br.com.academia.api.student;

import java.util.List;
import java.util.UUID;

import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.student.HouseholdDtos.CreateHouseholdRequest;
import br.com.academia.api.student.HouseholdDtos.HouseholdDetail;
import br.com.academia.api.student.HouseholdDtos.HouseholdSummary;
import br.com.academia.api.student.HouseholdDtos.RenameHouseholdRequest;
import br.com.academia.api.student.HouseholdDtos.StudentIdRequest;
import br.com.academia.api.student.StudentDtos.StudentDetail;
import br.com.academia.api.student.StudentDtos.StudentRequest;
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
@RequestMapping("/households")
@PreAuthorize("hasAnyRole('OWNER','CO_MANAGER','INSTRUCTOR')")
public class HouseholdController {

	private static final String MANAGERS = "hasAnyRole('OWNER','CO_MANAGER')";

	private final HouseholdService service;

	public HouseholdController(HouseholdService service) {
		this.service = service;
	}

	@GetMapping
	public List<HouseholdSummary> list() {
		return service.list(academyId());
	}

	@GetMapping("/{id}")
	public HouseholdDetail get(@PathVariable UUID id) {
		return service.get(academyId(), id);
	}

	@PostMapping
	@PreAuthorize(MANAGERS)
	@ResponseStatus(HttpStatus.CREATED)
	public HouseholdDetail create(@Valid @RequestBody CreateHouseholdRequest body) {
		return service.create(academyId(), body);
	}

	@PutMapping("/{id}")
	@PreAuthorize(MANAGERS)
	public HouseholdDetail rename(@PathVariable UUID id, @Valid @RequestBody RenameHouseholdRequest body) {
		return service.rename(academyId(), id, body.name());
	}

	@PutMapping("/{id}/responsible")
	@PreAuthorize(MANAGERS)
	public HouseholdDetail setResponsible(@PathVariable UUID id, @Valid @RequestBody StudentIdRequest body) {
		return service.setResponsible(academyId(), id, body.studentId());
	}

	@PostMapping("/{id}/members")
	@PreAuthorize(MANAGERS)
	public HouseholdDetail addMember(@PathVariable UUID id, @Valid @RequestBody StudentIdRequest body) {
		return service.addMember(academyId(), id, body.studentId());
	}

	@DeleteMapping("/{id}/members/{studentId}")
	@PreAuthorize(MANAGERS)
	public HouseholdDetail removeMember(@PathVariable UUID id, @PathVariable UUID studentId) {
		return service.removeMember(academyId(), id, studentId);
	}

	@PostMapping("/{id}/dependents")
	@PreAuthorize(MANAGERS)
	@ResponseStatus(HttpStatus.CREATED)
	public StudentDetail addDependent(@PathVariable UUID id, @Valid @RequestBody StudentRequest body) {
		return service.addDependent(academyId(), id, body);
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
