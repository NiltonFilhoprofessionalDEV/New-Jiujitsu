package br.com.academia.api.platform;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.academia.api.platform.AcademyCreationInvite.Status;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface AcademyCreationInviteRepository extends JpaRepository<AcademyCreationInvite, UUID> {

	Optional<AcademyCreationInvite> findByToken(String token);

	/** Trava a linha: dois aceites simultâneos não usam o mesmo convite. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select i from AcademyCreationInvite i where i.token = ?1")
	Optional<AcademyCreationInvite> findForUpdateByToken(String token);

	List<AcademyCreationInvite> findByStatusAndExpiresAtAfterOrderByCreatedAtDesc(Status status, Instant now);
}
