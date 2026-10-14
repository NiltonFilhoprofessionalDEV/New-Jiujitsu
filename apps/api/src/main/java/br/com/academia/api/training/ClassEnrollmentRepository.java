package br.com.academia.api.training;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import br.com.academia.api.student.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ClassEnrollmentRepository extends JpaRepository<ClassEnrollment, UUID> {

	boolean existsByClassIdAndStudentId(UUID classId, UUID studentId);

	long countByClassId(UUID classId);

	@Query("""
			select new br.com.academia.api.training.ClassCount(e.classId, count(e))
			from ClassEnrollment e where e.classId in ?1 group by e.classId
			""")
	List<ClassCount> countByClassIds(Collection<UUID> classIds);

	@Query("""
			select s from Student s
			where s.id in (select e.studentId from ClassEnrollment e where e.classId = ?1)
			order by s.searchName
			""")
	List<Student> findEnrolledStudents(UUID classId);

	@Modifying
	@Query("delete from ClassEnrollment e where e.classId = ?1 and e.studentId = ?2")
	int deleteEnrollment(UUID classId, UUID studentId);
}
