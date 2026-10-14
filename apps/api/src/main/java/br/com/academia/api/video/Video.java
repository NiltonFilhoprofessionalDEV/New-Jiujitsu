package br.com.academia.api.video;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import br.com.academia.api.common.TextNormalizer;
import br.com.academia.api.video.VideoEnums.Audience;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "videos")
public class Video {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "academy_id", nullable = false, updatable = false)
	private UUID academyId;

	@Column(name = "youtube_id", nullable = false)
	private String youtubeId;

	@Column(nullable = false)
	private boolean vertical;

	@Column(nullable = false)
	private String title;

	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Audience audience;

	/** Slugs das faixas; só valem com {@link Audience#BELTS}. */
	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(nullable = false, columnDefinition = "text[]")
	private List<String> belts = new ArrayList<>();

	/** Só valem com {@link Audience#CLASSES}. */
	@ElementCollection
	@CollectionTable(name = "video_audience_classes", joinColumns = @JoinColumn(name = "video_id"))
	@Column(name = "class_id")
	private Set<UUID> classIds = new HashSet<>();

	@Column(name = "created_by", updatable = false)
	private UUID createdBy;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt = Instant.now();

	protected Video() {
	}

	Video(UUID academyId, UUID createdBy) {
		this.academyId = academyId;
		this.createdBy = createdBy;
	}

	@PreUpdate
	void touch() {
		updatedAt = Instant.now();
	}

	/** Valores já validados pelo service: público coerente com faixas e turmas, link já convertido. */
	void apply(YoutubeLink link, String title, String description, Audience audience, List<String> belts,
			Set<UUID> classIds) {
		this.youtubeId = link.id();
		this.vertical = link.vertical();
		this.title = title.trim();
		this.description = TextNormalizer.blankToNull(description);
		this.audience = audience;
		this.belts = audience == Audience.BELTS ? new ArrayList<>(belts) : new ArrayList<>();
		this.classIds.clear();
		if (audience == Audience.CLASSES) {
			this.classIds.addAll(classIds);
		}
	}

	public UUID getId() {
		return id;
	}

	public UUID getAcademyId() {
		return academyId;
	}

	public String getYoutubeId() {
		return youtubeId;
	}

	public boolean isVertical() {
		return vertical;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public Audience getAudience() {
		return audience;
	}

	public List<String> getBelts() {
		return belts;
	}

	public Set<UUID> getClassIds() {
		return classIds;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
