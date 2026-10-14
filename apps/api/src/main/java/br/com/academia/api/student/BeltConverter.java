package br.com.academia.api.student;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BeltConverter implements AttributeConverter<Belt, String> {

	@Override
	public String convertToDatabaseColumn(Belt belt) {
		return belt == null ? null : belt.slug();
	}

	@Override
	public Belt convertToEntityAttribute(String slug) {
		return slug == null ? null : Belt.fromSlug(slug);
	}
}
