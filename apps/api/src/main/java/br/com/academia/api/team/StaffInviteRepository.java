package br.com.academia.api.team;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.academia.api.team.StaffInvite.Status;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface StaffInviteRepository extends JpaRepository<StaffInvite, UUID> {

	Optional<StaffInvite> findByToken(String token);

	/** Trava a linha: dois aceites simultâneos não usam o mesmo convite. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select i from StaffInvite i where i.token = ?1")
	Optional<StaffInvite> findForUpdateByToken(String token);

	Optional<StaffInvite> findByIdAndAcademyId(UUID id, UUID academyId);

	List<StaffInvite> findByAcademyIdAndStatusAndExpiresAtAfterOrderByCreatedAtDesc(
			UUID academyId, Status status, Instant now);
}
