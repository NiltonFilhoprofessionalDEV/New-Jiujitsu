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

	/** Dados de cadastro e telefones como digitados; {@code null} quando em branco. */
	public record PersonInput(String name, String phone, String emergencyContact, String emergencyPhone) {
	}

	/** Nomes formatados e telefones como digitados ({@code null} quando em branco). */
	public record Person(String name, String phone, String emergencyContact, String emergencyPhone) {
	}

	/**
	 * Exige nome e sobrenome e telefones com DDD. O contato de emergência (nome e telefone) é obrigatório para
	 * quem treina; o responsável que não treina pode deixar em branco, mas se preencher um, precisa do outro.
	 * Junta todos os erros, para o formulário mostrar tudo de uma vez.
	 */
	public static Person person(Kind kind, PersonInput in) {
		String name = PersonName.format(in.name());
		String phone = TextNormalizer.blankToNull(in.phone());
		String contact = TextNormalizer.blankToNull(PersonName.format(in.emergencyContact()));
		String emergencyPhone = TextNormalizer.blankToNull(in.emergencyPhone());

		List<FieldError> errors = new ArrayList<>();
		if (PersonName.isPhoneLike(name)) {
			errors.add(PersonName.phoneInName("name"));
		} else if (!PersonName.isFull(name)) {
			errors.add(new FieldError("name", "FULL_NAME_REQUIRED", "Informe nome e sobrenome"));
		}
		if (phone != null && !Phone.isValid(phone)) {
			errors.add(invalidPhone("phone"));
		}
		boolean emergencyRequired = kind == Kind.TRAINING || contact != null || emergencyPhone != null;
		if (emergencyRequired && contact == null) {
			errors.add(new FieldError("emergencyContact", "EMERGENCY_CONTACT_REQUIRED",
					"Informe o nome do contato de emergência"));
		} else if (contact != null && PersonName.isPhoneLike(contact)) {
			errors.add(PersonName.phoneInName("emergencyContact"));
		}
		if (emergencyRequired && emergencyPhone == null) {
			errors.add(new FieldError("emergencyPhone", "EMERGENCY_PHONE_REQUIRED", "Informe um telefone de emergência"));
		} else if (emergencyPhone != null && !Phone.isValid(emergencyPhone)) {
			errors.add(invalidPhone("emergencyPhone"));
		}
		if (!errors.isEmpty()) {
			throw ApiException.invalidFields(errors);
		}
		return new Person(name, phone, contact, emergencyPhone);
	}

	private static FieldError invalidPhone(String field) {
		return new FieldError(field, "INVALID_PHONE", "Telefone inválido: informe com DDD");
	}
}
