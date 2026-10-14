package br.com.academia.api.common;

import java.util.List;

import org.springframework.data.domain.Page;

/** Contrato estável de paginação (mesmos nomes de campos do Spring Data), sem expor {@code PageImpl}. */
public record PageResponse<T>(List<T> content, long totalElements, int totalPages, int number, int size) {

	public static <T> PageResponse<T> of(Page<T> page) {
		return new PageResponse<>(page.getContent(), page.getTotalElements(), page.getTotalPages(),
				page.getNumber(), page.getSize());
	}
}
