package br.com.academia.api.graduation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import br.com.academia.api.student.Belt;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Sem graduação registrada, a faixa conta desde a matrícula e o grau desde "grau concedido em"
 * (ou a matrícula). Treinos no próprio dia da graduação não contam para a próxima; na matrícula, contam.
 */
@Repository
class ProgressQuery {

	private static final String SQL = """
			with last_any as (
			    select distinct on (g.student_id) g.student_id, g.graduated_on, coalesce(g.prior_trainings, 0) as prior
			    from graduations g where g.academy_id = :academyId
			    order by g.student_id, g.graduated_on desc, g.created_at desc),
			last_belt as (
			    select distinct on (g.student_id) g.student_id, g.graduated_on, coalesce(g.prior_trainings, 0) as prior
			    from graduations g where g.academy_id = :academyId and g.kind = 'BELT'
			    order by g.student_id, g.graduated_on desc, g.created_at desc),
			base as (
			    select s.id, s.name, s.nickname, s.belt, s.degrees,
			           coalesce(lb.graduated_on, s.enrollment_date - 1) as belt_after,
			           coalesce(lb.graduated_on, s.enrollment_date) as belt_since,
			           coalesce(lb.prior, 0) as belt_prior,
			           coalesce(la.graduated_on, s.degree_awarded_at, s.enrollment_date - 1) as last_after,
			           coalesce(la.graduated_on, s.degree_awarded_at, s.enrollment_date) as last_since,
			           coalesce(la.prior, 0) as last_prior
			    from students s
			    left join last_any la on la.student_id = s.id
			    left join last_belt lb on lb.student_id = s.id
			    where s.academy_id = :academyId and s.kind = 'TRAINING' and s.status = 'ACTIVE'
			      and s.belt is not null and (cast(:studentId as uuid) is null or s.id = cast(:studentId as uuid)))
			select b.id, b.name, b.nickname, b.belt, b.degrees, b.belt_since, b.last_since,
			       b.belt_prior + (select count(*) from check_ins ci join class_sessions cs on cs.id = ci.session_id
			                       where ci.student_id = b.id and cs.session_date > b.belt_after) as trainings_since_belt,
			       b.last_prior + (select count(*) from check_ins ci join class_sessions cs on cs.id = ci.session_id
			                       where ci.student_id = b.id and cs.session_date > b.last_after) as trainings_since_last
			from base b
			order by b.name
			""";

	private final JdbcClient jdbc;

	ProgressQuery(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	List<ProgressRow> forAcademy(UUID academyId) {
		return run(academyId, null);
	}

	List<ProgressRow> forStudent(UUID academyId, UUID studentId) {
		return run(academyId, studentId);
	}

	private List<ProgressRow> run(UUID academyId, UUID studentId) {
		return jdbc.sql(SQL)
				.param("academyId", academyId)
				.param("studentId", studentId)
				.query(ProgressQuery::map)
				.list();
	}

	private static ProgressRow map(ResultSet rs, int row) throws SQLException {
		return new ProgressRow(
				rs.getObject("id", UUID.class),
				rs.getString("name"),
				rs.getString("nickname"),
				Belt.fromSlug(rs.getString("belt")),
				rs.getInt("degrees"),
				rs.getObject("belt_since", LocalDate.class),
				rs.getLong("trainings_since_belt"),
				rs.getObject("last_since", LocalDate.class),
				rs.getLong("trainings_since_last"));
	}
}
