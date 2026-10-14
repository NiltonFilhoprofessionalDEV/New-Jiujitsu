package br.com.academia.api.training;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ClassSessionRepository extends JpaRepository<ClassSession, UUID> {

	Optional<ClassSession> findByClassIdAndSessionDate(UUID classId, LocalDate sessionDate);

	/** Idempotente: duas chamadas salvas ao mesmo tempo não colidem na unicidade (aula, data). */
	@Modifying(flushAutomatically = true)
	@Query(value = """
			insert into class_sessions (id, academy_id, class_id, session_date, created_at, updated_at)
			values (gen_random_uuid(), ?1, ?2, ?3, now(), now())
			on conflict (class_id, session_date) do nothing
			""", nativeQuery = true)
	int insertIfAbsent(UUID academyId, UUID classId, LocalDate sessionDate);
}
