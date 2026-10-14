package br.com.academia.api.student;

/** Publicado antes de excluir o aluno; quem escuta pode vetar lançando {@code ApiException}. */
public record StudentDeletingEvent(Student student) {
}
