package br.com.academia.api.invite;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.academia.api.invite.EnrollmentRequest.Status;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface EnrollmentRequestRepository extends JpaRepository<EnrollmentRequest, UUID> {

	List<EnrollmentRequest> findByAcademyIdAndStatusOrderByCreatedAtAsc(UUID academyId, Status status);

	long countByAcademyIdAndStatus(UUID academyId, Status status);

	/** Trava a linha: duas decisões simultâneas não criam dois alunos. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select r from EnrollmentRequest r where r.id = ?1 and r.academyId = ?2")
	Optional<EnrollmentRequest> findForUpdate(UUID id, UUID academyId);
}
