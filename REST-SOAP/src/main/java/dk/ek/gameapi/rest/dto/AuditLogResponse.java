package dk.ek.gameapi.rest.dto;

import dk.ek.gameapi.entity.AuditLog;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        String tableName,
        String operation,
        String rowPk,
        LocalDateTime changedAt,
        String changedBy,
        String oldData,
        String newData
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getTableName(),
                log.getOperation(),
                log.getRowPk(),
                log.getChangedAt(),
                log.getChangedBy(),
                log.getOldData(),
                log.getNewData()
        );
    }
}
