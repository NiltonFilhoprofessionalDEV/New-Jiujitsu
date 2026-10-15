package br.com.academia.api.academy;

import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface AcademyRepository extends JpaRepository<Academy, UUID> {

	boolean existsBySlug(String slug);

	/** Serializa a assinatura: dois cliques em "Assinar" não criam duas assinaturas no Asaas. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select a from Academy a where a.id = ?1")
	Optional<Academy> findForUpdate(UUID id);

	/** Trava a academia do webhook: eventos simultâneos da mesma assinatura não se sobrescrevem. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select a from Academy a where a.asaasSubscriptionId = ?1")
	Optional<Academy> findForUpdateByAsaasSubscription(String subscriptionId);
}
