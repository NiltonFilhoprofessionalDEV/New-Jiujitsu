package br.com.academia.api.membership;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {

	@Query("""
			select m from Membership m join fetch m.academy
			where m.user.id = ?1 and m.active = true
			order by m.lastUsedAt desc nulls last, m.createdAt asc
			""")
	List<Membership> findActiveByUser(UUID userId);

	@Query("select m from Membership m join fetch m.academy join fetch m.user where m.id = ?1 and m.active = true")
	Optional<Membership> findActiveById(UUID id);

	@Query("""
			select m from Membership m join fetch m.user
			where m.academy.id = ?1 and m.active = true and m.role in ?2
			order by m.user.name
			""")
	List<Membership> findActiveInAcademyByRoles(UUID academyId, Collection<MembershipRole> roles);

	@Query("select m from Membership m join fetch m.user where m.id = ?1 and m.academy.id = ?2 and m.active = true")
	Optional<Membership> findActiveInAcademy(UUID id, UUID academyId);

	@Query("""
			select count(m) > 0 from Membership m
			where m.user.id = ?1 and m.academy.id = ?2 and m.role in ?3
			""")
	boolean existsInAcademyWithRoles(UUID userId, UUID academyId, Collection<MembershipRole> roles);

	@Query("""
			select m from Membership m join fetch m.academy join fetch m.user
			where m.user.id = ?1 and m.academy.id = ?2
			""")
	List<Membership> findByUserAndAcademy(UUID userId, UUID academyId);
}
