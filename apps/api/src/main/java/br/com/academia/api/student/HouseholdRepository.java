package br.com.academia.api.student;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface HouseholdRepository extends JpaRepository<Household, UUID> {

	Optional<Household> findByIdAndAcademyId(UUID id, UUID academyId);

	@Query("select h from Household h where h.academyId = ?1 order by h.searchName")
	List<Household> findAllInAcademy(UUID academyId);

	@Modifying
	@Query("update Student s set s.household = null where s.household.id = ?1")
	void detachMembers(UUID householdId);
}
