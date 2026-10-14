package br.com.academia.api.video;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.common.SlidingWindowLimiter;
import br.com.academia.api.video.VideoDtos.CommentView;
import br.com.academia.api.video.VideoDtos.VideoView;
import br.com.academia.api.video.VideoEnums.Reaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reações, favoritos, "assistido" e comentários. Todo acesso passa pelo público do vídeo: quem não pode
 * vê-lo recebe 404. Comentários publicam na hora; o autor ou um gestor pode apagar.
 */
@Service
public class VideoEngagementService {

	static final int MAX_COMMENTS_PER_WINDOW = 10;
	static final Duration COMMENT_WINDOW = Duration.ofMinutes(5);

	private final VideoService videos;
	private final VideoViewers viewers;
	private final VideoInteractions interactions;
	private final SlidingWindowLimiter commentLimiter = new SlidingWindowLimiter(MAX_COMMENTS_PER_WINDOW,
			COMMENT_WINDOW);

	public VideoEngagementService(VideoService videos, VideoViewers viewers, VideoInteractions interactions) {
		this.videos = videos;
		this.viewers = viewers;
		this.interactions = interactions;
	}

	@Transactional
	public VideoView react(CurrentUser me, UUID videoId, Reaction reaction) {
		Video video = visible(me, videoId);
		if (reaction == null) {
			interactions.clearReaction(videoId, me.membershipId());
		} else {
			interactions.react(videoId, me.membershipId(), reaction);
		}
		return videos.view(me, video);
	}

	@Transactional
	public VideoView favorite(CurrentUser me, UUID videoId, boolean favorite) {
		Video video = visible(me, videoId);
		if (favorite) {
			interactions.favorite(videoId, me.membershipId());
		} else {
			interactions.unfavorite(videoId, me.membershipId());
		}
		return videos.view(me, video);
	}

	@Transactional
	public VideoView markWatched(CurrentUser me, UUID videoId) {
		Video video = visible(me, videoId);
		interactions.markWatched(videoId, me.membershipId());
		return videos.view(me, video);
	}

	@Transactional(readOnly = true)
	public List<CommentView> comments(CurrentUser me, UUID videoId) {
		VideoViewer viewer = viewers.of(me);
		videos.requireVisible(viewer, videoId);
		return interactions.comments(videoId).stream().map(c -> {
			boolean mine = c.membershipId().equals(me.membershipId());
			return new CommentView(c.id(), c.authorName(), c.authorRole(), mine, mine || viewer.manager(), c.body(),
					c.createdAt());
		}).toList();
	}

	@Transactional
	public List<CommentView> addComment(CurrentUser me, UUID videoId, String body) {
		visible(me, videoId);
		String key = me.membershipId().toString();
		commentLimiter.check(key);
		interactions.addComment(videoId, me.membershipId(), body.strip());
		commentLimiter.record(key);
		return comments(me, videoId);
	}

	@Transactional
	public List<CommentView> deleteComment(CurrentUser me, UUID videoId, UUID commentId) {
		visible(me, videoId);
		UUID author = interactions.commentAuthor(videoId, commentId)
				.orElseThrow(() -> ApiException.notFound("COMMENT_NOT_FOUND", "Comentário não encontrado"));
		if (!author.equals(me.membershipId()) && !me.isManager()) {
			throw ApiException.forbidden("COMMENT_NOT_YOURS", "Só o autor ou um gestor pode apagar o comentário");
		}
		interactions.deleteComment(commentId);
		return comments(me, videoId);
	}

	private Video visible(CurrentUser me, UUID videoId) {
		return videos.requireVisible(viewers.of(me), videoId);
	}
}
