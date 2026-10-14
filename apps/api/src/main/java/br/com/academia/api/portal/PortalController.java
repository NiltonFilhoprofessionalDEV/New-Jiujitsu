package br.com.academia.api.portal;

import java.util.List;
import java.util.UUID;

import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.portal.PortalDtos.InvoiceItem;
import br.com.academia.api.portal.PortalDtos.Overview;
import br.com.academia.api.portal.PortalDtos.Profile;
import br.com.academia.api.training.ScheduleDtos.TodayClass;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Área do aluno: o aluno e, se responsável, os dependentes. Check-in devolve as aulas de hoje atualizadas. */
@RestController
@RequestMapping("/portal")
@PreAuthorize("hasAnyRole('STUDENT','RESPONSIBLE')")
public class PortalController {

	private final PortalService portal;

	public PortalController(PortalService portal) {
		this.portal = portal;
	}

	@GetMapping("/profiles")
	public List<Profile> profiles() {
		return portal.profiles(CurrentUser.get());
	}

	@GetMapping("/students/{studentId}")
	public Overview overview(@PathVariable UUID studentId) {
		return portal.overview(CurrentUser.get(), studentId);
	}

	@GetMapping("/students/{studentId}/invoices")
	public List<InvoiceItem> invoices(@PathVariable UUID studentId) {
		return portal.invoices(CurrentUser.get(), studentId);
	}

	@GetMapping("/students/{studentId}/today")
	public List<TodayClass> today(@PathVariable UUID studentId) {
		return portal.today(CurrentUser.get(), studentId);
	}

	@PostMapping("/students/{studentId}/check-ins/{classId}")
	public List<TodayClass> checkIn(@PathVariable UUID studentId, @PathVariable UUID classId) {
		return portal.checkIn(CurrentUser.get(), studentId, classId);
	}

	@DeleteMapping("/students/{studentId}/check-ins/{classId}")
	public List<TodayClass> undoCheckIn(@PathVariable UUID studentId, @PathVariable UUID classId) {
		return portal.undoCheckIn(CurrentUser.get(), studentId, classId);
	}
}
