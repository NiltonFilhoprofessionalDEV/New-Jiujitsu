package br.com.academia.api.common;

import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(ApiException.class)
	ProblemDetail handleApi(ApiException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.status(), ex.getMessage());
		problem.setProperty("code", ex.code());
		if (!ex.fields().isEmpty()) {
			problem.setProperty("fields", ex.fields());
		}
		return problem;
	}

	@ExceptionHandler(AccessDeniedException.class)
	ProblemDetail handleAccessDenied(AccessDeniedException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Acesso negado");
		problem.setProperty("code", "FORBIDDEN");
		return problem;
	}

	/** Corrida entre duas requisições que passaram pela checagem de duplicidade ao mesmo tempo. */
	@ExceptionHandler(DataIntegrityViolationException.class)
	ProblemDetail handleIntegrity(DataIntegrityViolationException ex) {
		log.warn("Violação de integridade: {}", ex.getMostSpecificCause().getClass().getSimpleName());
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Registro duplicado ou em uso");
		problem.setProperty("code", "CONFLICT");
		return problem;
	}

	@ExceptionHandler(Exception.class)
	ProblemDetail handleUnexpected(Exception ex) {
		log.error("Erro inesperado", ex);
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno");
		problem.setProperty("code", "INTERNAL_ERROR");
		return problem;
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		Map<String, String> fields = ex.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.toMap(f -> f.getField(), f -> String.valueOf(f.getDefaultMessage()), (a, b) -> a));
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Dados inválidos");
		problem.setProperty("code", "VALIDATION_ERROR");
		problem.setProperty("fields", fields);
		return ResponseEntity.badRequest().body(problem);
	}
}
