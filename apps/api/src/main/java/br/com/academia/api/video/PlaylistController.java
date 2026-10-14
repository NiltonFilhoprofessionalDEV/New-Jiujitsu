package br.com.academia.api.video;

import java.util.List;
import java.util.UUID;

import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.video.VideoDtos.PlaylistRequest;
import br.com.academia.api.video.VideoDtos.PlaylistView;
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
@RequestMapping("/playlists")
@PreAuthorize("hasAnyRole('OWNER','CO_MANAGER','INSTRUCTOR','STUDENT','RESPONSIBLE')")
public class PlaylistController {

	private final PlaylistService service;

	public PlaylistController(PlaylistService service) {
		this.service = service;
	}

	@GetMapping
	public List<PlaylistView> list() {
		return service.list(CurrentUser.get());
	}

	@PostMapping
	@PreAuthorize(VideoController.MANAGERS)
	@ResponseStatus(HttpStatus.CREATED)
	public PlaylistView create(@Valid @RequestBody PlaylistRequest body) {
		return service.create(CurrentUser.get(), body);
	}

	@PutMapping("/{id}")
	@PreAuthorize(VideoController.MANAGERS)
	public PlaylistView update(@PathVariable UUID id, @Valid @RequestBody PlaylistRequest body) {
		return service.update(CurrentUser.get(), id, body);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize(VideoController.MANAGERS)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		service.delete(CurrentUser.get(), id);
	}
}
