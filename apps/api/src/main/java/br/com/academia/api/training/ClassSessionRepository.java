package br.com.academia.api.training;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ClassSessionRepository extends JpaRepository<ClassSession, UUID> {

	Optional<ClassSession> findByClassIdAndSessionDate(UUID classId, LocalDate sessionDate);

	@Query("""
			select s.classId from ClassSession s
			where s.academyId = ?1 and s.sessionDate = ?2 and s.extra = true
			""")
	List<UUID> findExtraClassIds(UUID academyId, LocalDate sessionDate);

	/** Idempotente: duas chamadas salvas ao mesmo tempo não colidem na unicidade (aula, data). */
	@Modifying(flushAutomatically = true)
	@Query(value = """
			insert into class_sessions (id, academy_id, class_id, session_date, extra, created_at, updated_at)
			values (gen_random_uuid(), ?1, ?2, ?3, false, now(), now())
			on conflict (class_id, session_date) do nothing
			""", nativeQuery = true)
	int insertIfAbsent(UUID academyId, UUID classId, LocalDate sessionDate);

	/** Abre a chamada fora da grade; se a sessão já existir, mantém o que já está. */
	@Modifying(flushAutomatically = true)
	@Query(value = """
			insert into class_sessions (id, academy_id, class_id, session_date, extra, created_at, updated_at)
			values (gen_random_uuid(), ?1, ?2, ?3, true, now(), now())
			on conflict (class_id, session_date) do nothing
			""", nativeQuery = true)
	int insertExtraIfAbsent(UUID academyId, UUID classId, LocalDate sessionDate);
}
