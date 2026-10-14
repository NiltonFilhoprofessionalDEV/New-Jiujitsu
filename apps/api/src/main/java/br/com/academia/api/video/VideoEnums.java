package br.com.academia.api.video;

public final class VideoEnums {

	private VideoEnums() {
	}

	/** Quem vê o vídeo além da equipe: todos os alunos, só algumas faixas ou só algumas turmas. */
	public enum Audience {
		ALL, BELTS, CLASSES
	}

	/** Conjunto fixo de reações; o frontend decide o emoji de cada uma. */
	public enum Reaction {
		LIKE, FIRE, CLAP, STRONG, LAUGH
	}
}
