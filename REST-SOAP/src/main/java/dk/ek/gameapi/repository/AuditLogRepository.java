package dk.ek.gameapi.repository;

import dk.ek.gameapi.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByTableName(String tableName, Pageable pageable);

    Page<AuditLog> findByOperation(String operation, Pageable pageable);

    Page<AuditLog> findByTableNameAndOperation(String tableName, String operation, Pageable pageable);
}
