package br.com.academia.api.training;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface TrainingClassRepository extends JpaRepository<TrainingClass, UUID> {

	@Query("""
			select c from TrainingClass c
			where c.academyId = ?1 and c.archivedAt is null
			order by c.startTime, c.title
			""")
	List<TrainingClass> findActive(UUID academyId);

	@Query("select c from TrainingClass c where c.id = ?1 and c.academyId = ?2 and c.archivedAt is null")
	Optional<TrainingClass> findActiveInAcademy(UUID id, UUID academyId);

	/** Trava a aula para a checagem de capacidade não ser furada por inscrições simultâneas. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select c from TrainingClass c where c.id = ?1 and c.academyId = ?2 and c.archivedAt is null")
	Optional<TrainingClass> findForUpdate(UUID id, UUID academyId);
}
