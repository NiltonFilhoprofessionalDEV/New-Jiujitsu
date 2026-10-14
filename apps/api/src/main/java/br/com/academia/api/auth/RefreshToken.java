package br.com.academia.api.auth;

import java.time.Instant;
import java.util.UUID;

import br.com.academia.api.membership.Membership;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "membership_id")
	private Membership membership;

	@Column(name = "token_hash", nullable = false, unique = true, length = 64)
	private String tokenHash;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "revoked_at")
	private Instant revokedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	protected RefreshToken() {
	}

	public RefreshToken(Membership membership, String tokenHash, Instant expiresAt) {
		this.userId = membership.getUser().getId();
		this.membership = membership;
		this.tokenHash = tokenHash;
		this.expiresAt = expiresAt;
	}

	public boolean isUsable(Instant now) {
		return revokedAt == null && expiresAt.isAfter(now);
	}

	public boolean isRevoked() {
		return revokedAt != null;
	}

	public void revoke() {
		if (revokedAt == null) {
			revokedAt = Instant.now();
		}
	}

	public UUID getUserId() {
		return userId;
	}

	public Membership getMembership() {
		return membership;
	}
}
