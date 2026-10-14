package br.com.academia.api.training;

import java.util.UUID;

/** Projeção de contagem por aula (inscritos ou presentes). */
public record ClassCount(UUID classId, long total) {
}
