package br.com.academia.api.video;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PlaylistRepository extends JpaRepository<Playlist, UUID> {

	@Query("select distinct p from Playlist p left join fetch p.videoIds where p.academyId = ?1 order by p.createdAt")
	List<Playlist> findAllInAcademy(UUID academyId);

	@Query("select p from Playlist p where p.id = ?1 and p.academyId = ?2")
	Optional<Playlist> findInAcademy(UUID id, UUID academyId);

	@Query("select distinct p from Playlist p join p.videoIds v where p.academyId = ?1 and v = ?2")
	List<Playlist> findContaining(UUID academyId, UUID videoId);
}
