package br.com.academia.api.academy;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AcademyProfile(
		@NotBlank @Size(max = 120) String name,
		@Pattern(regexp = "^$|^\\d{2}\\.?\\d{3}\\.?\\d{3}/?\\d{4}-?\\d{2}$", message = "CNPJ inválido") String cnpj,
		@Email @Size(max = 254) String email,
		@Size(max = 30) String phone,
		@Size(max = 255) String address,
		@Size(max = 120) String city,
		@Pattern(regexp = "^$|^[A-Z]{2}$", message = "UF inválida") String state) {
}
