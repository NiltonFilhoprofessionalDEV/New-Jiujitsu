package br.com.academia.api.student;

import java.util.ArrayList;
import java.util.List;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.FieldError;
import br.com.academia.api.common.PersonName;
import br.com.academia.api.common.Phone;
import br.com.academia.api.common.TextNormalizer;
import br.com.academia.api.student.StudentEnums.Kind;

/** Regras de cadastro iguais para o gestor, o link de matrícula e o convite. */
public final class StudentRules {

	private StudentRules() {
	}

	/** Nome formatado e telefones como digitados ({@code null} quando em branco). */
	public record Person(String name, String phone, String emergencyPhone) {
	}

	/**
	 * Exige nome e sobrenome e telefones com DDD; o de emergência é obrigatório para quem treina (o responsável
	 * que não treina pode deixar em branco). Junta todos os erros, para o formulário mostrar tudo de uma vez.
	 */
	public static Person person(Kind kind, String rawName, String rawPhone, String rawEmergencyPhone) {
		String name = PersonName.format(rawName);
		String phone = TextNormalizer.blankToNull(rawPhone);
		String emergencyPhone = TextNormalizer.blankToNull(rawEmergencyPhone);

		List<FieldError> errors = new ArrayList<>();
		if (!PersonName.isFull(name)) {
			errors.add(new FieldError("name", "FULL_NAME_REQUIRED", "Informe nome e sobrenome"));
		}
		if (phone != null && !Phone.isValid(phone)) {
			errors.add(invalidPhone("phone"));
		}
		if (emergencyPhone == null && kind == Kind.TRAINING) {
			errors.add(new FieldError("emergencyPhone", "EMERGENCY_PHONE_REQUIRED", "Informe um telefone de emergência"));
		} else if (emergencyPhone != null && !Phone.isValid(emergencyPhone)) {
			errors.add(invalidPhone("emergencyPhone"));
		}
		if (!errors.isEmpty()) {
			throw ApiException.invalidFields(errors);
		}
		return new Person(name, phone, emergencyPhone);
	}

	private static FieldError invalidPhone(String field) {
		return new FieldError(field, "INVALID_PHONE", "Telefone inválido: informe com DDD");
	}
}
