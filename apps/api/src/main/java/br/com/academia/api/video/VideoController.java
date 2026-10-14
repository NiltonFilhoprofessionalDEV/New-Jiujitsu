package br.com.academia.api.video;

import java.util.List;
import java.util.UUID;

import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.video.VideoDtos.CommentRequest;
import br.com.academia.api.video.VideoDtos.CommentView;
import br.com.academia.api.video.VideoDtos.ReactionRequest;
import br.com.academia.api.video.VideoDtos.VideoRequest;
import br.com.academia.api.video.VideoDtos.VideoView;
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

/** Biblioteca de vídeos: o gestor cadastra; equipe, alunos e responsáveis assistem e interagem. */
@RestController
@RequestMapping("/videos")
@PreAuthorize("hasAnyRole('OWNER','CO_MANAGER','INSTRUCTOR','STUDENT','RESPONSIBLE')")
public class VideoController {

	static final String MANAGERS = "hasAnyRole('OWNER','CO_MANAGER')";

	private final VideoService videos;
	private final VideoEngagementService engagement;

	public VideoController(VideoService videos, VideoEngagementService engagement) {
		this.videos = videos;
		this.engagement = engagement;
	}

	@GetMapping
	public List<VideoView> list() {
		return videos.list(CurrentUser.get());
	}

	@GetMapping("/{id}")
	public VideoView get(@PathVariable UUID id) {
		return videos.get(CurrentUser.get(), id);
	}

	@PostMapping
	@PreAuthorize(MANAGERS)
	@ResponseStatus(HttpStatus.CREATED)
	public VideoView create(@Valid @RequestBody VideoRequest body) {
		return videos.create(CurrentUser.get(), body);
	}

	@PutMapping("/{id}")
	@PreAuthorize(MANAGERS)
	public VideoView update(@PathVariable UUID id, @Valid @RequestBody VideoRequest body) {
		return videos.update(CurrentUser.get(), id, body);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize(MANAGERS)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		videos.delete(CurrentUser.get(), id);
	}

	@PutMapping("/{id}/reaction")
	public VideoView react(@PathVariable UUID id, @RequestBody ReactionRequest body) {
		return engagement.react(CurrentUser.get(), id, body.reaction());
	}

	@PutMapping("/{id}/favorite")
	public VideoView favorite(@PathVariable UUID id) {
		return engagement.favorite(CurrentUser.get(), id, true);
	}

	@DeleteMapping("/{id}/favorite")
	public VideoView unfavorite(@PathVariable UUID id) {
		return engagement.favorite(CurrentUser.get(), id, false);
	}

	@PutMapping("/{id}/watched")
	public VideoView watched(@PathVariable UUID id) {
		return engagement.markWatched(CurrentUser.get(), id);
	}

	@GetMapping("/{id}/comments")
	public List<CommentView> comments(@PathVariable UUID id) {
		return engagement.comments(CurrentUser.get(), id);
	}

	@PostMapping("/{id}/comments")
	@ResponseStatus(HttpStatus.CREATED)
	public List<CommentView> addComment(@PathVariable UUID id, @Valid @RequestBody CommentRequest body) {
		return engagement.addComment(CurrentUser.get(), id, body.body());
	}

	@DeleteMapping("/{id}/comments/{commentId}")
	public List<CommentView> deleteComment(@PathVariable UUID id, @PathVariable UUID commentId) {
		return engagement.deleteComment(CurrentUser.get(), id, commentId);
	}
}
