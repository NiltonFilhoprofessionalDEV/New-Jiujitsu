package br.com.academia.api.invite;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.academia.api.invite.Invite.Status;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface InviteRepository extends JpaRepository<Invite, UUID> {

	Optional<Invite> findByToken(String token);

	/** Trava a linha até o fim da transação: dois aceites simultâneos não usam o mesmo convite. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select i from Invite i where i.token = ?1")
	Optional<Invite> findForUpdateByToken(String token);

	Optional<Invite> findByIdAndAcademyId(UUID id, UUID academyId);

	List<Invite> findByAcademyIdOrderByCreatedAtDesc(UUID academyId, Limit limit);

	List<Invite> findByAcademyIdAndStatusOrderByCreatedAtDesc(UUID academyId, Status status, Limit limit);

	List<Invite> findByAcademyIdAndStatusAndExpiresAtAfterOrderByCreatedAtDesc(
			UUID academyId, Status status, Instant now, Limit limit);

	List<Invite> findByAcademyIdAndStatusAndExpiresAtLessThanEqualOrderByCreatedAtDesc(
			UUID academyId, Status status, Instant now, Limit limit);
}
