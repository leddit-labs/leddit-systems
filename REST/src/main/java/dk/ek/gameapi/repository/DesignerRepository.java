package dk.ek.gameapi.repository;

import dk.ek.gameapi.entity.Designer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignerRepository extends JpaRepository<Designer, Integer> {

    Page<Designer> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
