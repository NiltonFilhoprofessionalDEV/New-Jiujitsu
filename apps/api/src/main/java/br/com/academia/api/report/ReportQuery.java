package br.com.academia.api.report;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import br.com.academia.api.report.ReportDtos.Absent;
import br.com.academia.api.report.ReportDtos.AbsentRow;
import br.com.academia.api.report.ReportDtos.AttendanceStats;
import br.com.academia.api.report.ReportDtos.Period;
import br.com.academia.api.report.ReportDtos.StudentRow;
import br.com.academia.api.report.ReportDtos.StudentStats;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Somas de leitura para os relatórios; todo valor vindo do usuário entra por parâmetro. */
@Repository
class ReportQuery {

	/** Contagens por turma; o nome do professor é resolvido depois, pela equipe atual. */
	record ClassCounts(UUID classId, String title, UUID instructorId, long sessions, long checkIns) {
	}

	record InstructorCounts(UUID membershipId, long classes, long sessions, long checkIns) {
	}

	private record AbsentHit(AbsentRow row, long total) {
	}

	private static final String CHECK_INS = " from check_ins c join class_sessions s on s.id = c.session_id ";
	private static final String IN_PERIOD =
			" where s.academy_id = :academyId and s.session_date between :from and :to ";

	private final JdbcClient jdbc;

	ReportQuery(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	StudentStats students(UUID academyId, Period period) {
		return inPeriod("""
				select count(*) filter (where status = 'ACTIVE'), count(*) filter (where status = 'INACTIVE'),
				       count(*) filter (where enrollment_date between :from and :to)
				from students where academy_id = :academyId and kind = 'TRAINING'
				""", academyId, period)
				.query((rs, n) -> new StudentStats(rs.getLong(1), rs.getLong(2), rs.getLong(3)))
				.single();
	}

	AttendanceStats attendance(UUID academyId, Period period) {
		return inPeriod("select count(*), count(distinct c.session_id), count(distinct c.student_id)"
				+ CHECK_INS + IN_PERIOD, academyId, period)
				.query((rs, n) -> new AttendanceStats(rs.getLong(1), rs.getLong(2), rs.getLong(3)))
				.single();
	}

	List<ClassCounts> classes(UUID academyId, Period period) {
		return inPeriod("select t.id, t.title, t.instructor_membership_id, count(distinct c.session_id), count(*)"
				+ CHECK_INS + "join training_classes t on t.id = s.class_id" + IN_PERIOD + """
				group by t.id, t.title, t.instructor_membership_id
				order by count(*) desc, t.title
				""", academyId, period)
				.query((rs, n) -> new ClassCounts(rs.getObject(1, UUID.class), rs.getString(2),
						rs.getObject(3, UUID.class), rs.getLong(4), rs.getLong(5)))
				.list();
	}

	List<InstructorCounts> instructors(UUID academyId, Period period) {
		return inPeriod("""
				select t.instructor_membership_id, count(distinct t.id) filter (where t.archived_at is null),
				       count(distinct c.session_id), count(c.id)
				from training_classes t
				left join class_sessions s on s.class_id = t.id and s.session_date between :from and :to
				left join check_ins c on c.session_id = s.id
				where t.academy_id = :academyId and t.instructor_membership_id is not null
				group by t.instructor_membership_id
				""", academyId, period)
				.query((rs, n) -> new InstructorCounts(rs.getObject(1, UUID.class), rs.getLong(2), rs.getLong(3),
						rs.getLong(4)))
				.list();
	}

	List<StudentRow> topStudents(UUID academyId, Period period, int limit) {
		return inPeriod("select st.id, st.name, st.belt, st.degrees, count(*)"
				+ CHECK_INS + "join students st on st.id = c.student_id" + IN_PERIOD + """
				group by st.id, st.name, st.belt, st.degrees, st.search_name
				order by count(*) desc, st.search_name
				limit :limit
				""", academyId, period)
				.param("limit", limit)
				.query((rs, n) -> new StudentRow(rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3),
						rs.getInt(4), rs.getLong(5)))
				.list();
	}

	/** Matriculados antes de {@code since} (quem acabou de entrar não conta como sumido). */
	Absent absent(UUID academyId, LocalDate since, int limit) {
		List<AbsentHit> hits = jdbc.sql("""
				select st.id, st.name, max(s.session_date), count(*) over ()
				from students st
				left join check_ins c on c.student_id = st.id
				left join class_sessions s on s.id = c.session_id
				where st.academy_id = :academyId and st.kind = 'TRAINING' and st.status = 'ACTIVE'
				  and st.enrollment_date < :since
				group by st.id, st.name, st.search_name
				having max(s.session_date) is null or max(s.session_date) < :since
				order by max(s.session_date) nulls first, st.search_name
				limit :limit
				""")
				.param("academyId", academyId)
				.param("since", since)
				.param("limit", limit)
				.query((rs, n) -> new AbsentHit(new AbsentRow(rs.getObject(1, UUID.class), rs.getString(2),
						rs.getObject(3, LocalDate.class)), rs.getLong(4)))
				.list();
		long total = hits.isEmpty() ? 0 : hits.getFirst().total();
		return new Absent(since, total, hits.stream().map(AbsentHit::row).toList());
	}

	private JdbcClient.StatementSpec inPeriod(String sql, UUID academyId, Period period) {
		return jdbc.sql(sql)
				.param("academyId", academyId)
				.param("from", period.from())
				.param("to", period.to());
	}
}
