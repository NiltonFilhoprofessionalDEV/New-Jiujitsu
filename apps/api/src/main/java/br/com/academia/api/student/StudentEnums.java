package br.com.academia.api.student;

public final class StudentEnums {

	private StudentEnums() {
	}

	/** {@code RESPONSIBLE} é o responsável que não treina: sem faixa e sem mensalidade. */
	public enum Kind {
		TRAINING, RESPONSIBLE
	}

	public enum Status {
		ACTIVE, INACTIVE
	}

	/** Régua de graduação: automática pela idade ou fixada pelo gestor. */
	public enum BeltRuler {
		AUTO, KIDS, ADULT
	}

	public enum Gender {
		MALE, FEMALE
	}
}
