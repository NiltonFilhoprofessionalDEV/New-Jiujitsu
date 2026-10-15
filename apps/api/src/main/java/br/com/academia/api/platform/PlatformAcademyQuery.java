package br.com.academia.api.platform;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.academia.api.platform.PlatformDtos.AcademyRow;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Visão de todas as academias para o admin: dono (o primeiro cadastrado) e alunos ativos. */
@Repository
class PlatformAcademyQuery {

	private static final String SELECT = """
			select a.id, a.name, a.slug, a.created_at, a.trial_ends_at, a.paid_until,
			       a.asaas_subscription_id is not null as subscribed, o.name as owner_name, o.email as owner_email,
			       (select count(*) from students s where s.academy_id = a.id and s.status = 'ACTIVE') as active_students
			from academies a
			left join lateral (
			    select u.name, u.email from memberships m join users u on u.id = m.user_id
			    where m.academy_id = a.id and m.role = 'OWNER'
			    order by m.created_at limit 1
			) o on true
			""";

	private final JdbcClient jdbc;

	PlatformAcademyQuery(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	List<AcademyRow> all() {
		return jdbc.sql(SELECT + " order by a.created_at desc").query(PlatformAcademyQuery::row).list();
	}

	Optional<AcademyRow> find(UUID academyId) {
		return jdbc.sql(SELECT + " where a.id = :id").param("id", academyId).query(PlatformAcademyQuery::row).optional();
	}

	private static AcademyRow row(ResultSet rs, int n) throws SQLException {
		return new AcademyRow(
				rs.getObject("id", UUID.class),
				rs.getString("name"),
				rs.getString("slug"),
				instant(rs.getTimestamp("created_at")),
				instant(rs.getTimestamp("trial_ends_at")),
				instant(rs.getTimestamp("paid_until")),
				rs.getBoolean("subscribed"),
				rs.getString("owner_name"),
				rs.getString("owner_email"),
				rs.getLong("active_students"));
	}

	private static Instant instant(Timestamp ts) {
		return ts == null ? null : ts.toInstant();
	}
}
