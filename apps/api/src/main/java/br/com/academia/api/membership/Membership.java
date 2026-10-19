package br.com.academia.api.membership;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.academia.api.academy.Academy;
import br.com.academia.api.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "memberships")
public class Membership {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "academy_id")
	private Academy academy;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MembershipRole role;

	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(nullable = false, columnDefinition = "text[]")
	private List<String> permissions = new ArrayList<>();

	@Column(nullable = false)
	private boolean active = true;

	/** Vínculo criado pelo admin da plataforma para entrar na academia sem aparecer na equipe. */
	@Column(name = "platform_support", nullable = false)
	private boolean platformSupport = false;

	@Column(name = "last_used_at")
	private Instant lastUsedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	protected Membership() {
	}

	public Membership(User user, Academy academy, MembershipRole role) {
		this.user = user;
		this.academy = academy;
		this.role = role;
	}

	/** Vínculo criado inativo: só entra depois que a academia aprovar. */
	public static Membership pending(User user, Academy academy, MembershipRole role) {
		Membership membership = new Membership(user, academy, role);
		membership.active = false;
		return membership;
	}

	/** Dono oculto: o admin da plataforma entra na academia sem constar na equipe. */
	public static Membership platformSupport(User user, Academy academy) {
		Membership membership = new Membership(user, academy, MembershipRole.OWNER);
		membership.platformSupport = true;
		return membership;
	}

	public void activate() {
		this.active = true;
	}

	/** Vale a partir do próximo token de acesso (no máximo o TTL do token atual). */
	public void changeRole(MembershipRole newRole) {
		this.role = newRole;
	}

	public UUID getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public Academy getAcademy() {
		return academy;
	}

	public MembershipRole getRole() {
		return role;
	}

	public List<String> getPermissions() {
		return permissions;
	}

	public boolean isActive() {
		return active;
	}

	public boolean isPlatformSupport() {
		return platformSupport;
	}

	public Instant getLastUsedAt() {
		return lastUsedAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void markUsed() {
		this.lastUsedAt = Instant.now();
	}
}
