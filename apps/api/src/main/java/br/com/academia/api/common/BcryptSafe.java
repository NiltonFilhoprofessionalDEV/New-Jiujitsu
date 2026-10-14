package br.com.academia.api.common;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.nio.charset.StandardCharsets;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

/**
 * O BCrypt só aceita até 72 bytes e o encoder lança exceção acima disso. {@code @Size} conta caracteres:
 * 60 letras acentuadas passam nele e ocupam 120 bytes. Nulo é responsabilidade de {@code @NotBlank}.
 */
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
@Constraint(validatedBy = BcryptSafe.Validator.class)
public @interface BcryptSafe {

	int MAX_BYTES = 72;

	String message() default "A senha é longa demais (letras acentuadas contam em dobro)";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	class Validator implements ConstraintValidator<BcryptSafe, String> {

		@Override
		public boolean isValid(String value, ConstraintValidatorContext context) {
			return value == null || value.getBytes(StandardCharsets.UTF_8).length <= MAX_BYTES;
		}
	}
}
