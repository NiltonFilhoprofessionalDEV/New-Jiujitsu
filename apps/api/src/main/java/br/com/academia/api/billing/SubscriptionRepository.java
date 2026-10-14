package br.com.academia.api.billing;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

	List<Subscription> findByAcademyIdAndCanceledAtIsNull(UUID academyId);

	boolean existsByStudentIdAndCanceledAtIsNull(UUID studentId);

	/** Serializa emissão (rotina diária) e edição da mesma mensalidade. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from Subscription s where s.id = ?1 and s.academyId = ?2 and s.canceledAt is null")
	Optional<Subscription> findActiveForUpdate(UUID id, UUID academyId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from Subscription s where s.id = ?1 and s.canceledAt is null")
	Optional<Subscription> findActiveForUpdate(UUID id);

	@Query("select s.id from Subscription s where s.canceledAt is null and s.nextDueDate <= ?1")
	List<UUID> findDueIds(LocalDate horizon);

	@Query("""
			select new br.com.academia.api.billing.PlanCount(s.planId, count(s))
			from Subscription s where s.academyId = ?1 and s.canceledAt is null group by s.planId
			""")
	List<PlanCount> countActiveByPlan(UUID academyId);
}
