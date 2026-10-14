package br.com.academia.api.video;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import br.com.academia.api.common.TextNormalizer;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "playlists")
public class Playlist {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "academy_id", nullable = false, updatable = false)
	private UUID academyId;

	@Column(nullable = false)
	private String title;

	private String description;

	/** Na ordem definida pelo gestor. */
	@ElementCollection
	@CollectionTable(name = "playlist_videos", joinColumns = @JoinColumn(name = "playlist_id"))
	@OrderColumn(name = "position")
	@Column(name = "video_id")
	private List<UUID> videoIds = new ArrayList<>();

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt = Instant.now();

	protected Playlist() {
	}

	Playlist(UUID academyId) {
		this.academyId = academyId;
	}

	@PreUpdate
	void touch() {
		updatedAt = Instant.now();
	}

	void rename(String title, String description) {
		this.title = title.trim();
		this.description = TextNormalizer.blankToNull(description);
	}

	/** Lista já validada pelo service: vídeos da academia, sem repetição. */
	void reorder(List<UUID> orderedVideoIds) {
		videoIds.clear();
		videoIds.addAll(orderedVideoIds);
	}

	void append(UUID videoId) {
		if (!videoIds.contains(videoId)) {
			videoIds.add(videoId);
		}
	}

	void remove(UUID videoId) {
		videoIds.remove(videoId);
	}

	public UUID getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	/** Ignora buracos na ordem, caso uma linha tenha sido apagada direto no banco. */
	public List<UUID> getVideoIds() {
		return videoIds.stream().filter(Objects::nonNull).toList();
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
