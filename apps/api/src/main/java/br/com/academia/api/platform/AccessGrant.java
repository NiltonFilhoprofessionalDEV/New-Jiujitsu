package br.com.academia.api.platform;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Registro de auditoria de cada liberação feita pelo admin da plataforma. */
@Entity
@Table(name = "academy_access_grants")
public class AccessGrant {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "academy_id", nullable = false, updatable = false)
	private UUID academyId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false)
	private GrantKind kind;

	@Column(nullable = false, updatable = false)
	private int days;

	@Column(updatable = false)
	private String note;

	@Column(name = "granted_by", nullable = false, updatable = false)
	private UUID grantedBy;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	protected AccessGrant() {
	}

	public AccessGrant(UUID academyId, GrantKind kind, int days, String note, UUID grantedBy) {
		this.academyId = academyId;
		this.kind = kind;
		this.days = days;
		this.note = note;
		this.grantedBy = grantedBy;
	}
}
