package br.com.academia.api.video;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VideoRepository extends JpaRepository<Video, UUID> {

	@Query("select distinct v from Video v left join fetch v.classIds where v.academyId = ?1 order by v.createdAt desc")
	List<Video> findAllInAcademy(UUID academyId);

	@Query("select v from Video v left join fetch v.classIds where v.id = ?1 and v.academyId = ?2")
	Optional<Video> findInAcademy(UUID id, UUID academyId);

	@Query("select v.id from Video v where v.academyId = ?1 and v.id in ?2")
	List<UUID> findIdsInAcademy(UUID academyId, Collection<UUID> ids);

	boolean existsByAcademyIdAndYoutubeId(UUID academyId, String youtubeId);

	boolean existsByAcademyIdAndYoutubeIdAndIdNot(UUID academyId, String youtubeId, UUID id);
}
