package br.com.academia.api.video;

import java.util.Set;
import java.util.UUID;

/**
 * Quem está assistindo. A equipe vê todos os vídeos; aluno e responsável veem pelo público de cada vídeo,
 * somando as faixas e turmas dos perfis ativos da conta (o próprio e, para o responsável, os dependentes).
 */
record VideoViewer(UUID academyId, UUID membershipId, boolean staff, boolean manager, boolean active,
		Set<String> belts, Set<UUID> classIds) {

	boolean canSee(Video video) {
		if (staff) {
			return true;
		}
		if (!active) {
			return false;
		}
		return switch (video.getAudience()) {
			case ALL -> true;
			case BELTS -> video.getBelts().stream().anyMatch(belts::contains);
			case CLASSES -> video.getClassIds().stream().anyMatch(classIds::contains);
		};
	}
}
