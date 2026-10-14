package br.com.academia.api.student;

/** Publicado na mesma transação do cadastro; quem escuta pode completar o aluno (ex.: plano padrão). */
public record StudentCreatedEvent(Student student) {
}
