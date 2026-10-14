package br.com.academia.api.billing;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PlanRepository extends JpaRepository<Plan, UUID> {

	List<Plan> findByAcademyIdOrderByNameAsc(UUID academyId);

	Optional<Plan> findByIdAndAcademyId(UUID id, UUID academyId);

	Optional<Plan> findByAcademyIdAndIsDefaultTrue(UUID academyId);

	Optional<Plan> findByAcademyIdAndIsKidsDefaultTrue(UUID academyId);

	/** Roda antes de marcar o novo padrão: o índice único parcial não admite dois ao mesmo tempo. */
	@Modifying(flushAutomatically = true)
	@Query("update Plan p set p.isDefault = false where p.academyId = ?1 and p.isDefault = true")
	void clearDefault(UUID academyId);

	@Modifying(flushAutomatically = true)
	@Query("update Plan p set p.isKidsDefault = false where p.academyId = ?1 and p.isKidsDefault = true")
	void clearKidsDefault(UUID academyId);
}
