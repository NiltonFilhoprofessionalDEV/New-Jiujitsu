package br.com.academia.api.access;

/** SituaÃ§Ã£o da academia na plataforma. SÃ³ {@link #SUSPENDED} bloqueia a equipe. */
public enum AccessStatus {
	TRIAL, ACTIVE, GRACE, SUSPENDED
}
