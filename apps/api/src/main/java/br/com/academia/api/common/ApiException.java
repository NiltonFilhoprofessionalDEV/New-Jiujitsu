package br.com.academia.api.common;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;

/** Erro esperado de regra de negócio, convertido em ProblemDetail com um código estável para o frontend. */
public class ApiException extends RuntimeException {

	private final HttpStatus status;
	private final String code;
	/** Mensagem por campo do formulário, para o frontend destacar; vazio quando o erro não é de campo. */
	private final Map<String, String> fields;

	public ApiException(HttpStatus status, String code, String message) {
		this(status, code, message, Map.of());
	}

	private ApiException(HttpStatus status, String code, String message, Map<String, String> fields) {
		super(message);
		this.status = status;
		this.code = code;
		this.fields = fields;
	}

	/** 400 com todos os campos inválidos de uma vez; código e mensagem gerais vêm do primeiro. */
	public static ApiException invalidFields(List<FieldError> errors) {
		if (errors.isEmpty()) {
			throw new IllegalArgumentException("Sem erros de campo");
		}
		Map<String, String> fields = new LinkedHashMap<>();
		errors.forEach(e -> fields.putIfAbsent(e.field(), e.message()));
		FieldError first = errors.getFirst();
		return new ApiException(HttpStatus.BAD_REQUEST, first.code(), first.message(),
				Collections.unmodifiableMap(fields));
	}

	public static ApiException notFound(String code, String message) {
		return new ApiException(HttpStatus.NOT_FOUND, code, message);
	}

	public static ApiException conflict(String code, String message) {
		return new ApiException(HttpStatus.CONFLICT, code, message);
	}

	public static ApiException unauthorized(String code, String message) {
		return new ApiException(HttpStatus.UNAUTHORIZED, code, message);
	}

	public static ApiException forbidden(String code, String message) {
		return new ApiException(HttpStatus.FORBIDDEN, code, message);
	}

	public static ApiException badRequest(String code, String message) {
		return new ApiException(HttpStatus.BAD_REQUEST, code, message);
	}

	public static ApiException tooManyRequests(String code, String message) {
		return new ApiException(HttpStatus.TOO_MANY_REQUESTS, code, message);
	}

	public static ApiException unavailable(String code, String message) {
		return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, code, message);
	}

	public HttpStatus status() {
		return status;
	}

	public String code() {
		return code;
	}

	public Map<String, String> fields() {
		return fields;
	}
}
