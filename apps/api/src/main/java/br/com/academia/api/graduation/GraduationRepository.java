package br.com.academia.api.graduation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GraduationRepository extends JpaRepository<Graduation, UUID> {

	List<Graduation> findByStudentIdOrderByGraduatedOnDescCreatedAtDesc(UUID studentId);

	Optional<Graduation> findFirstByStudentIdOrderByGraduatedOnDescCreatedAtDesc(UUID studentId);

	Optional<Graduation> findByIdAndStudentId(UUID id, UUID studentId);
}
