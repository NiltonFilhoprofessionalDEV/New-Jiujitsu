package br.com.academia.api.academy;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademyRepository extends JpaRepository<Academy, UUID> {

	boolean existsBySlug(String slug);
}
