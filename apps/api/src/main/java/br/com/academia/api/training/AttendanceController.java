package br.com.academia.api.training;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.training.AttendanceDtos.DayClass;
import br.com.academia.api.training.AttendanceDtos.SaveSessionRequest;
import br.com.academia.api.training.AttendanceDtos.SessionView;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Chamada: gestores e professores marcam presença. */
@RestController
@RequestMapping("/attendance")
@PreAuthorize("hasAnyRole('OWNER','CO_MANAGER','INSTRUCTOR')")
public class AttendanceController {

	private final AttendanceService service;

	public AttendanceController(AttendanceService service) {
		this.service = service;
	}

	@GetMapping("/day")
	public List<DayClass> day(@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
		return service.day(CurrentUser.get().academyId(), date);
	}

	@GetMapping("/classes/{classId}/sessions/{date}")
	public SessionView session(@PathVariable UUID classId, @PathVariable @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
		return service.session(CurrentUser.get().academyId(), classId, date);
	}

	@PutMapping("/classes/{classId}/sessions/{date}")
	public SessionView save(@PathVariable UUID classId, @PathVariable @DateTimeFormat(iso = ISO.DATE) LocalDate date,
			@Valid @RequestBody SaveSessionRequest body) {
		CurrentUser user = CurrentUser.get();
		return service.save(user.academyId(), user.membershipId(), classId, date, body);
	}
}
