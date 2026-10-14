package br.com.academia.api.auth;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

	@Query("select t from RefreshToken t join fetch t.membership m join fetch m.academy join fetch m.user where t.tokenHash = ?1")
	Optional<RefreshToken> findByTokenHash(String tokenHash);

	@Modifying
	@Query("update RefreshToken t set t.revokedAt = current_timestamp where t.userId = ?1 and t.revokedAt is null")
	int revokeAllForUser(UUID userId);

	/** Apagar (e não revogar): token revogado reapresentado conta como roubo e derruba a sessão nova. */
	@Modifying
	@Query("delete from RefreshToken t where t.userId = ?1")
	int deleteAllForUser(UUID userId);
}
