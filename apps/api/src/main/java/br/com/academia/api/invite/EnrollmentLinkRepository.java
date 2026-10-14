package br.com.academia.api.invite;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentLinkRepository extends JpaRepository<EnrollmentLink, UUID> {

	Optional<EnrollmentLink> findByToken(String token);
}
