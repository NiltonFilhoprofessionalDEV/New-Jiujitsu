package br.com.academia.api.video;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import br.com.academia.api.membership.MembershipRole;
import br.com.academia.api.student.Belt;
import br.com.academia.api.video.VideoEnums.Audience;
import br.com.academia.api.video.VideoEnums.Reaction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class VideoDtos {

	static final int MAX_CLASSES = 50;
	static final int MAX_PLAYLISTS = 50;
	static final int MAX_PLAYLIST_VIDEOS = 300;
	static final int MAX_COMMENT_LENGTH = 1000;

	private VideoDtos() {
	}

	/** {@code playlistIds}: playlists que passam a conter o vídeo (ele entra no fim de cada uma). */
	public record VideoRequest(
			@NotBlank(message = "Cole o link do vídeo") @Size(max = YoutubeLink.MAX_URL_LENGTH) String url,
			@NotBlank(message = "Dê um título ao vídeo") @Size(max = 120) String title,
			@Size(max = 2000) String description,
			@NotNull Audience audience,
			@Size(max = 20) List<@NotBlank String> belts,
			@Size(max = MAX_CLASSES) List<@NotNull UUID> classIds,
			@Size(max = MAX_PLAYLISTS) List<@NotNull UUID> playlistIds) {

		List<String> beltsOrEmpty() {
			return belts == null ? List.of() : belts.stream().distinct().map(b -> Belt.fromSlug(b).slug()).toList();
		}

		List<UUID> classIdsOrEmpty() {
			return classIds == null ? List.of() : classIds.stream().distinct().toList();
		}

		List<UUID> playlistIdsOrEmpty() {
			return playlistIds == null ? List.of() : playlistIds.stream().distinct().toList();
		}
	}

	public record VideoView(
			UUID id,
			String youtubeId,
			boolean vertical,
			String title,
			String description,
			Instant createdAt,
			Audience audience,
			List<String> belts,
			List<UUID> classIds,
			Map<Reaction, Long> reactions,
			Reaction myReaction,
			long commentCount,
			boolean favorite,
			boolean watched) {
	}

	/** {@code reaction} nulo remove a reação. */
	public record ReactionRequest(Reaction reaction) {
	}

	public record CommentRequest(@NotBlank(message = "Escreva um comentário") @Size(max = MAX_COMMENT_LENGTH) String body) {
	}

	public record CommentView(UUID id, String authorName, MembershipRole authorRole, boolean mine, boolean canDelete,
			String body, Instant createdAt) {
	}

	public record PlaylistRequest(
			@NotBlank(message = "Dê um nome à playlist") @Size(max = 80) String title,
			@Size(max = 500) String description,
			@Size(max = MAX_PLAYLIST_VIDEOS) List<@NotNull UUID> videoIds) {

		List<UUID> videoIdsOrEmpty() {
			return videoIds == null ? List.of() : videoIds.stream().distinct().toList();
		}
	}

	/** {@code videoIds}: só os vídeos que quem pergunta pode ver, na ordem da playlist. */
	public record PlaylistView(UUID id, String title, String description, List<UUID> videoIds) {
	}
}
