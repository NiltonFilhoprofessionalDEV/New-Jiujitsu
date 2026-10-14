package br.com.academia.api.training;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CheckInRepository extends JpaRepository<CheckIn, UUID> {

	@Query("select c.studentId from CheckIn c where c.sessionId = ?1")
	List<UUID> findStudentIds(UUID sessionId);

	@Query("""
			select new br.com.academia.api.training.ClassCount(s.classId, count(c))
			from CheckIn c, ClassSession s
			where c.sessionId = s.id and s.academyId = ?1 and s.sessionDate = ?2
			group by s.classId
			""")
	List<ClassCount> countPresentByClass(UUID academyId, LocalDate date);

	@Modifying
	@Query(value = """
			insert into check_ins (id, session_id, student_id, marked_by, created_at)
			values (gen_random_uuid(), ?1, ?2, ?3, now())
			on conflict (session_id, student_id) do nothing
			""", nativeQuery = true)
	int insertIfAbsent(UUID sessionId, UUID studentId, UUID markedBy);

	@Modifying
	@Query("delete from CheckIn c where c.sessionId = ?1 and c.studentId in ?2")
	int deleteStudents(UUID sessionId, Collection<UUID> studentIds);
}
