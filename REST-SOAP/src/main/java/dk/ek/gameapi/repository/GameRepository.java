
package dk.ek.gameapi.repository;

import dk.ek.gameapi.entity.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameRepository extends JpaRepository<Game, Integer> {
    // Search by name (prevents SQL injection by using JPA)
    Page<Game> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Game> findByAvailableTrue(Pageable pageable);

    @Query("SELECT g FROM Game g WHERE g.yearPublished BETWEEN :startYear AND :endYear")
    Page<Game> findByYearRange(@Param("startYear") Integer startYear,
                               @Param("endYear") Integer endYear,
                               Pageable pageable);
}