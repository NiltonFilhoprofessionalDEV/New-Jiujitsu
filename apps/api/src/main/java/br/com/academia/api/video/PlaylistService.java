package br.com.academia.api.video;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.video.VideoDtos.PlaylistRequest;
import br.com.academia.api.video.VideoDtos.PlaylistView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Playlists na ordem do gestor. Cada pessoa só recebe os vídeos que pode ver; playlists vazias somem. */
@Service
public class PlaylistService {

	private final PlaylistRepository playlists;
	private final VideoRepository videos;
	private final VideoViewers viewers;

	public PlaylistService(PlaylistRepository playlists, VideoRepository videos, VideoViewers viewers) {
		this.playlists = playlists;
		this.videos = videos;
		this.viewers = viewers;
	}

	@Transactional(readOnly = true)
	public List<PlaylistView> list(CurrentUser me) {
		VideoViewer viewer = viewers.of(me);
		Set<UUID> visible = new HashSet<>(videos.findAllInAcademy(me.academyId()).stream()
				.filter(viewer::canSee).map(Video::getId).toList());
		return playlists.findAllInAcademy(me.academyId()).stream()
				.map(p -> view(p, p.getVideoIds().stream().filter(visible::contains).toList()))
				.filter(p -> viewer.staff() || !p.videoIds().isEmpty())
				.toList();
	}

	@Transactional
	public PlaylistView create(CurrentUser me, PlaylistRequest request) {
		Playlist playlist = new Playlist(me.academyId());
		apply(me.academyId(), playlist, request);
		return view(playlists.save(playlist), playlist.getVideoIds());
	}

	@Transactional
	public PlaylistView update(CurrentUser me, UUID id, PlaylistRequest request) {
		Playlist playlist = require(me.academyId(), id);
		apply(me.academyId(), playlist, request);
		return view(playlist, playlist.getVideoIds());
	}

	/** Os vídeos continuam na biblioteca; só a playlist some. */
	@Transactional
	public void delete(CurrentUser me, UUID id) {
		playlists.delete(require(me.academyId(), id));
	}

	private void apply(UUID academyId, Playlist playlist, PlaylistRequest request) {
		List<UUID> videoIds = request.videoIdsOrEmpty();
		if (!videoIds.isEmpty() && videos.findIdsInAcademy(academyId, videoIds).size() != videoIds.size()) {
			throw VideoService.notFound();
		}
		playlist.rename(request.title(), request.description());
		playlist.reorder(videoIds);
	}

	private Playlist require(UUID academyId, UUID id) {
		return playlists.findInAcademy(id, academyId).orElseThrow(PlaylistService::notFound);
	}

	private static PlaylistView view(Playlist p, List<UUID> videoIds) {
		return new PlaylistView(p.getId(), p.getTitle(), p.getDescription(), videoIds);
	}

	static ApiException notFound() {
		return ApiException.notFound("PLAYLIST_NOT_FOUND", "Playlist não encontrada");
	}
}
