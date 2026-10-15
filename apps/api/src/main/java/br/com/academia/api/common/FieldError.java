package br.com.academia.api.common;

/** Erro de um campo do formulário: o frontend mostra {@code message} embaixo de {@code field}. */
public record FieldError(String field, String code, String message) {
}
