package br.com.academia.api.video;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.training.TrainingClass;
import br.com.academia.api.training.TrainingClassRepository;
import br.com.academia.api.video.VideoDtos.VideoRequest;
import br.com.academia.api.video.VideoDtos.VideoView;
import br.com.academia.api.video.VideoEnums.Audience;
import br.com.academia.api.video.VideoInteractions.Summary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cadastro dos vídeos (gestor) e a lista de cada pessoa, já filtrada pelo público de cada vídeo. */
@Service
public class VideoService {

	private final VideoRepository videos;
	private final PlaylistRepository playlists;
	private final TrainingClassRepository classes;
	private final VideoViewers viewers;
	private final VideoInteractions interactions;

	public VideoService(VideoRepository videos, PlaylistRepository playlists, TrainingClassRepository classes,
			VideoViewers viewers, VideoInteractions interactions) {
		this.videos = videos;
		this.playlists = playlists;
		this.classes = classes;
		this.viewers = viewers;
		this.interactions = interactions;
	}

	@Transactional(readOnly = true)
	public List<VideoView> list(CurrentUser me) {
		VideoViewer viewer = viewers.of(me);
		List<Video> visible = videos.findAllInAcademy(me.academyId()).stream().filter(viewer::canSee).toList();
		if (visible.isEmpty()) {
			return List.of();
		}
		Summary summary = interactions.summary(me.academyId(), me.membershipId(), null);
		return visible.stream().map(v -> view(v, summary)).toList();
	}

	@Transactional(readOnly = true)
	public VideoView get(CurrentUser me, UUID id) {
		return view(me, requireVisible(viewers.of(me), id));
	}

	@Transactional
	public VideoView create(CurrentUser me, VideoRequest request) {
		YoutubeLink link = YoutubeLink.parse(request.url());
		if (videos.existsByAcademyIdAndYoutubeId(me.academyId(), link.id())) {
			throw alreadyAdded();
		}
		Video video = new Video(me.academyId(), me.membershipId());
		apply(me.academyId(), video, link, request);
		videos.save(video);
		syncPlaylists(me.academyId(), video.getId(), request.playlistIdsOrEmpty());
		return view(me, video);
	}

	@Transactional
	public VideoView update(CurrentUser me, UUID id, VideoRequest request) {
		Video video = require(me.academyId(), id);
		YoutubeLink link = YoutubeLink.parse(request.url());
		if (videos.existsByAcademyIdAndYoutubeIdAndIdNot(me.academyId(), link.id(), id)) {
			throw alreadyAdded();
		}
		apply(me.academyId(), video, link, request);
		syncPlaylists(me.academyId(), id, request.playlistIdsOrEmpty());
		return view(me, video);
	}

	/** Tira o vídeo das playlists pela entidade, para a ordem das que restam continuar sem buracos. */
	@Transactional
	public void delete(CurrentUser me, UUID id) {
		Video video = require(me.academyId(), id);
		playlists.findContaining(me.academyId(), id).forEach(p -> p.remove(id));
		playlists.flush();
		videos.delete(video);
	}

	/** Vídeo que a pessoa pode ver; os demais respondem 404, sem revelar que existem. */
	Video requireVisible(VideoViewer viewer, UUID id) {
		Video video = require(viewer.academyId(), id);
		if (!viewer.canSee(video)) {
			throw notFound();
		}
		return video;
	}

	VideoView view(CurrentUser me, Video video) {
		return view(video, interactions.summary(me.academyId(), me.membershipId(), video.getId()));
	}

	private Video require(UUID academyId, UUID id) {
		return videos.findInAcademy(id, academyId).orElseThrow(VideoService::notFound);
	}

	private void apply(UUID academyId, Video video, YoutubeLink link, VideoRequest r) {
		List<String> belts = r.beltsOrEmpty();
		Set<UUID> classIds = new HashSet<>(r.classIdsOrEmpty());
		if (r.audience() == Audience.BELTS && belts.isEmpty()) {
			throw ApiException.badRequest("VIDEO_AUDIENCE_EMPTY", "Escolha ao menos uma faixa");
		}
		if (r.audience() == Audience.CLASSES) {
			if (classIds.isEmpty()) {
				throw ApiException.badRequest("VIDEO_AUDIENCE_EMPTY", "Escolha ao menos uma turma");
			}
			Set<UUID> active = new HashSet<>(classes.findActive(academyId).stream().map(TrainingClass::getId).toList());
			if (!active.containsAll(classIds)) {
				throw ApiException.notFound("CLASS_NOT_FOUND", "Turma não encontrada");
			}
		}
		video.apply(link, r.title(), r.description(), r.audience(), belts, classIds);
	}

	/** O vídeo passa a estar exatamente nas playlists pedidas; entra no fim das que ainda não o tinham. */
	private void syncPlaylists(UUID academyId, UUID videoId, List<UUID> wanted) {
		List<Playlist> all = playlists.findAllInAcademy(academyId);
		Set<UUID> known = new HashSet<>(all.stream().map(Playlist::getId).toList());
		if (!known.containsAll(wanted)) {
			throw PlaylistService.notFound();
		}
		all.forEach(p -> {
			if (wanted.contains(p.getId())) {
				p.append(videoId);
			} else {
				p.remove(videoId);
			}
		});
	}

	private static VideoView view(Video v, Summary s) {
		return new VideoView(v.getId(), v.getYoutubeId(), v.isVertical(), v.getTitle(), v.getDescription(),
				v.getCreatedAt(), v.getAudience(), List.copyOf(v.getBelts()), List.copyOf(v.getClassIds()),
				s.reactionsOf(v.getId()), s.mine().get(v.getId()), s.comments().getOrDefault(v.getId(), 0L),
				s.favorites().contains(v.getId()), s.watched().contains(v.getId()));
	}

	private static ApiException alreadyAdded() {
		return ApiException.conflict("VIDEO_ALREADY_ADDED", "Este vídeo já está na biblioteca");
	}

	static ApiException notFound() {
		return ApiException.notFound("VIDEO_NOT_FOUND", "Vídeo não encontrado");
	}
}
