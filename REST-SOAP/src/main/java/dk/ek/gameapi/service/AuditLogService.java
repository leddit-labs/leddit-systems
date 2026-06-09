package dk.ek.gameapi.service;

import dk.ek.gameapi.entity.AuditLog;
import dk.ek.gameapi.exception.ResourceNotFoundException;
import dk.ek.gameapi.repository.AuditLogRepository;
import dk.ek.gameapi.rest.dto.AuditLogResponse;
import dk.ek.gameapi.rest.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public PagedResponse<AuditLogResponse> getLogs(String tableName, String operation, Pageable pageable) {
        boolean hasTable = tableName != null && !tableName.isBlank();
        boolean hasOperation = operation != null && !operation.isBlank();

        Page<AuditLog> logs;
        if (hasTable && hasOperation) {
            logs = auditLogRepository.findByTableNameAndOperation(tableName, operation, pageable);
        } else if (hasTable) {
            logs = auditLogRepository.findByTableName(tableName, pageable);
        } else if (hasOperation) {
            logs = auditLogRepository.findByOperation(operation, pageable);
        } else {
            logs = auditLogRepository.findAll(pageable);
        }
        return PagedResponse.from(logs, AuditLogResponse::from);
    }

    public AuditLogResponse getLogById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid log ID: " + id);
        }
        AuditLog log = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AuditLog", id));
        return AuditLogResponse.from(log);
    }
}
