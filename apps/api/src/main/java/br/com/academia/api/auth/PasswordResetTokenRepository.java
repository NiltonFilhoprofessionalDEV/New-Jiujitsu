package br.com.academia.api.auth;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

	@Query("select t from PasswordResetToken t join fetch t.user where t.tokenHash = ?1")
	Optional<PasswordResetToken> findByTokenHash(String tokenHash);

	@Modifying
	@Query("update PasswordResetToken t set t.usedAt = ?2 where t.user.id = ?1 and t.usedAt is null")
	int invalidateOpen(UUID userId, Instant now);
}
