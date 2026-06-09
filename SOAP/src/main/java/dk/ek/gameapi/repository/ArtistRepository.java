package dk.ek.gameapi.repository;

import dk.ek.gameapi.entity.Artist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository extends JpaRepository<Artist, Integer> {

    Page<Artist> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
