package br.com.academia.api.student;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface StudentRepository extends JpaRepository<Student, UUID>, JpaSpecificationExecutor<Student> {

	@Query("select s from Student s left join fetch s.household where s.id = ?1 and s.academyId = ?2")
	Optional<Student> findInAcademy(UUID id, UUID academyId);

	/** Serializa graduações do mesmo aluno: cada uma grava o estado anterior que a próxima vai sobrescrever. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from Student s where s.id = ?1 and s.academyId = ?2")
	Optional<Student> findForUpdate(UUID id, UUID academyId);

	@Query("select s from Student s where s.academyId = ?1 and s.id in ?2")
	List<Student> findAllInAcademy(UUID academyId, List<UUID> ids);

	@Query("select s from Student s where s.household.id = ?1 order by s.searchName")
	List<Student> findMembers(UUID householdId);

	/** Cadastro de aluno ligado à conta de login na academia (área do aluno). */
	@Query("""
			select s from Student s left join fetch s.household
			where s.academyId = ?1 and s.userId = ?2 order by s.createdAt
			""")
	List<Student> findLinkedToUser(UUID academyId, UUID userId);

	@Query("""
			select new br.com.academia.api.student.HouseholdMemberCount(s.household.id, count(s))
			from Student s where s.academyId = ?1 and s.household is not null
			group by s.household.id
			""")
	List<HouseholdMemberCount> countMembersByHousehold(UUID academyId);

	boolean existsByAcademyIdAndEmail(UUID academyId, String email);

	boolean existsByAcademyIdAndEmailAndIdNot(UUID academyId, String email, UUID id);

	boolean existsByAcademyIdAndCpf(UUID academyId, String cpf);

	boolean existsByAcademyIdAndCpfAndIdNot(UUID academyId, String cpf, UUID id);
}
