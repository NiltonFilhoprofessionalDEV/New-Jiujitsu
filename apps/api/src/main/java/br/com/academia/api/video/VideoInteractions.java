package br.com.academia.api.video;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import br.com.academia.api.membership.MembershipRole;
import br.com.academia.api.video.VideoEnums.Reaction;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Curtidas, favoritos, "assistido" e comentários. Gravações idempotentes (on conflict): clique duplo ou
 * requisição repetida não geram erro nem linha duplicada.
 */
@Repository
class VideoInteractions {

	/** Totais por vídeo e o estado de quem pergunta. */
	record Summary(Map<UUID, Map<Reaction, Long>> reactions, Map<UUID, Reaction> mine, Map<UUID, Long> comments,
			Set<UUID> favorites, Set<UUID> watched) {

		Map<Reaction, Long> reactionsOf(UUID videoId) {
			return reactions.getOrDefault(videoId, Map.of());
		}
	}

	record CommentRow(UUID id, UUID membershipId, String authorName, MembershipRole authorRole, String body,
			Instant createdAt) {
	}

	private record ReactionCount(UUID videoId, Reaction reaction, long total) {
	}

	static final int COMMENT_LIMIT = 500;

	private static final String IN_ACADEMY = " join videos v on v.id = x.video_id where v.academy_id = :academyId ";
	private static final String ONE_VIDEO = " and v.id = :videoId ";

	private final JdbcClient jdbc;

	VideoInteractions(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	/** {@code videoId} nulo = todos os vídeos da academia. */
	Summary summary(UUID academyId, UUID membershipId, UUID videoId) {
		String scope = IN_ACADEMY + (videoId == null ? "" : ONE_VIDEO);
		Map<UUID, Map<Reaction, Long>> reactions = new HashMap<>();
		query("select x.video_id, x.reaction, count(*) from video_reactions x" + scope + "group by 1, 2",
				academyId, videoId)
				.query((rs, n) -> new ReactionCount(uuid(rs, 1), Reaction.valueOf(rs.getString(2)), rs.getLong(3)))
				.list()
				.forEach(c -> reactions.computeIfAbsent(c.videoId(), k -> new EnumMap<>(Reaction.class))
						.put(c.reaction(), c.total()));
		Map<UUID, Reaction> mine = new HashMap<>();
		mine(scope, "x.reaction from video_reactions x", academyId, membershipId, videoId)
				.query((rs, n) -> new ReactionCount(uuid(rs, 1), Reaction.valueOf(rs.getString(2)), 1))
				.list()
				.forEach(r -> mine.put(r.videoId(), r.reaction()));
		Map<UUID, Long> comments = new HashMap<>();
		query("select x.video_id, count(*) from video_comments x" + scope + "group by 1", academyId, videoId)
				.query((rs, n) -> Map.entry(uuid(rs, 1), rs.getLong(2)))
				.list()
				.forEach(e -> comments.put(e.getKey(), e.getValue()));
		return new Summary(reactions, mine, comments, ids(scope, "video_favorites", academyId, membershipId, videoId),
				ids(scope, "video_views", academyId, membershipId, videoId));
	}

	void react(UUID videoId, UUID membershipId, Reaction reaction) {
		jdbc.sql("""
				insert into video_reactions (video_id, membership_id, reaction) values (:videoId, :membershipId, :reaction)
				on conflict (video_id, membership_id) do update set reaction = excluded.reaction, created_at = now()
				""").param("videoId", videoId).param("membershipId", membershipId).param("reaction", reaction.name())
				.update();
	}

	void clearReaction(UUID videoId, UUID membershipId) {
		mutate("delete from video_reactions where video_id = :videoId and membership_id = :membershipId",
				videoId, membershipId);
	}

	void favorite(UUID videoId, UUID membershipId) {
		mutate("""
				insert into video_favorites (video_id, membership_id) values (:videoId, :membershipId)
				on conflict do nothing""", videoId, membershipId);
	}

	void unfavorite(UUID videoId, UUID membershipId) {
		mutate("delete from video_favorites where video_id = :videoId and membership_id = :membershipId",
				videoId, membershipId);
	}

	void markWatched(UUID videoId, UUID membershipId) {
		mutate("""
				insert into video_views (video_id, membership_id) values (:videoId, :membershipId)
				on conflict do nothing""", videoId, membershipId);
	}

	List<CommentRow> comments(UUID videoId) {
		return jdbc.sql("""
				select c.id, c.membership_id, u.name, m.role, c.body, c.created_at
				from video_comments c join memberships m on m.id = c.membership_id join users u on u.id = m.user_id
				where c.video_id = :videoId order by c.created_at limit :limit
				""").param("videoId", videoId).param("limit", COMMENT_LIMIT)
				.query((rs, n) -> new CommentRow(uuid(rs, 1), uuid(rs, 2), rs.getString(3),
						MembershipRole.valueOf(rs.getString(4)), rs.getString(5), instant(rs, 6)))
				.list();
	}

	void addComment(UUID videoId, UUID membershipId, String body) {
		jdbc.sql("insert into video_comments (video_id, membership_id, body) values (:videoId, :membershipId, :body)")
				.param("videoId", videoId).param("membershipId", membershipId).param("body", body).update();
	}

	/** Autor do comentário, se ele existir neste vídeo. */
	Optional<UUID> commentAuthor(UUID videoId, UUID commentId) {
		return jdbc.sql("select membership_id from video_comments where id = :id and video_id = :videoId")
				.param("id", commentId).param("videoId", videoId)
				.query((rs, n) -> uuid(rs, 1)).optional();
	}

	void deleteComment(UUID commentId) {
		jdbc.sql("delete from video_comments where id = :id").param("id", commentId).update();
	}

	private JdbcClient.StatementSpec query(String sql, UUID academyId, UUID videoId) {
		JdbcClient.StatementSpec spec = jdbc.sql(sql).param("academyId", academyId);
		return videoId == null ? spec : spec.param("videoId", videoId);
	}

	/** {@code table} é sempre uma constante desta classe, nunca texto vindo da requisição. */
	private Set<UUID> ids(String scope, String table, UUID academyId, UUID membershipId, UUID videoId) {
		return new HashSet<>(mine(scope, "1 from " + table + " x", academyId, membershipId, videoId)
				.query((rs, n) -> uuid(rs, 1)).list());
	}

	private JdbcClient.StatementSpec mine(String scope, String select, UUID academyId, UUID membershipId,
			UUID videoId) {
		return query("select x.video_id, " + select + scope + "and x.membership_id = :membershipId", academyId,
				videoId).param("membershipId", membershipId);
	}

	private void mutate(String sql, UUID videoId, UUID membershipId) {
		jdbc.sql(sql).param("videoId", videoId).param("membershipId", membershipId).update();
	}

	private static UUID uuid(ResultSet rs, int column) throws SQLException {
		return rs.getObject(column, UUID.class);
	}

	private static Instant instant(ResultSet rs, int column) throws SQLException {
		Timestamp timestamp = rs.getTimestamp(column);
		return timestamp == null ? null : timestamp.toInstant();
	}
}
