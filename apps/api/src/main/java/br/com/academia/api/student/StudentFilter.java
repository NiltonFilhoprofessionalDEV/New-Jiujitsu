package br.com.academia.api.student;

import static br.com.academia.api.common.EnumParams.parse;

/** Filtros da lista de alunos, no formato de query string usado pelo frontend (valores em minúsculas). */
public record StudentFilter(String q, StatusFilter status, KindFilter kind, AgeFilter age, HouseholdFilter household) {

	public enum StatusFilter {
		ACTIVE, INACTIVE, ALL
	}

	public enum KindFilter {
		ALL, TRAINING, RESPONSIBLE
	}

	/** {@code KIDS} até 15 anos e {@code ADULT} 16+ (régua de faixa); {@code UNDER18}/{@code ADULT18} pela idade legal. */
	public enum AgeFilter {
		ALL, KIDS, ADULT, UNDER18, ADULT18
	}

	public enum HouseholdFilter {
		ALL, NONE
	}

	public static StudentFilter of(String q, String status, String kind, String age, String household) {
		return new StudentFilter(
				q == null ? "" : q.trim(),
				parse(StatusFilter.class, status, StatusFilter.ACTIVE),
				parse(KindFilter.class, kind, KindFilter.ALL),
				parse(AgeFilter.class, age, AgeFilter.ALL),
				parse(HouseholdFilter.class, household, HouseholdFilter.ALL));
	}

	public StudentFilter withStatus(StatusFilter newStatus) {
		return new StudentFilter(q, newStatus, kind, age, household);
	}

	public StudentFilter withKind(KindFilter newKind) {
		return new StudentFilter(q, status, newKind, age, household);
	}
}
