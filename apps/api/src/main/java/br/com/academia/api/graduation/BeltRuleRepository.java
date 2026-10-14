package br.com.academia.api.graduation;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BeltRuleRepository extends JpaRepository<BeltRule, UUID> {

	List<BeltRule> findByAcademyId(UUID academyId);
}
