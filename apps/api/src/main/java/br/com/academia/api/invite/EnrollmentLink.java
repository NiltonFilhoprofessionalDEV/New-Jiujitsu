package br.com.academia.api.invite;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "enrollment_links")
public class EnrollmentLink {

	/** Estado visto por quem abre o link. */
	public enum State {
		OPEN, PAUSED, EXPIRED
	}

	@Id
	@Column(name = "academy_id")
	private UUID academyId;

	@Column(nullable = false, unique = true)
	private String token;

	@Column(nullable = false)
	private boolean enabled = true;

	@Column(name = "expires_on")
	private LocalDate expiresOn;

	@Column(nullable = false)
	private int opens;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt = Instant.now();

	protected EnrollmentLink() {
	}

	EnrollmentLink(UUID academyId, String token) {
		this.academyId = academyId;
		this.token = token;
	}

	@PreUpdate
	void touch() {
		updatedAt = Instant.now();
	}

	/** Troca o endereço: o token anterior deixa de valer na hora. */
	void rotate(String newToken) {
		this.token = newToken;
		this.opens = 0;
	}

	void configure(boolean enabled, LocalDate expiresOn) {
		this.enabled = enabled;
		this.expiresOn = expiresOn;
	}

	void registerOpen() {
		opens++;
	}

	State state(LocalDate today) {
		if (!enabled) {
			return State.PAUSED;
		}
		return expiresOn != null && expiresOn.isBefore(today) ? State.EXPIRED : State.OPEN;
	}

	public UUID getAcademyId() {
		return academyId;
	}

	public String getToken() {
		return token;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public LocalDate getExpiresOn() {
		return expiresOn;
	}

	public int getOpens() {
		return opens;
	}
}
