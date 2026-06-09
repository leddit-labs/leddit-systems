package dk.ek.gameapi.rest.controller;

import dk.ek.gameapi.rest.dto.AuditLogResponse;
import dk.ek.gameapi.rest.dto.PagedResponse;
import dk.ek.gameapi.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    private Pageable validatePageable(Pageable pageable) {
        if (pageable.getPageNumber() < 0 || pageable.getPageSize() <= 0) {
            throw new IllegalArgumentException("Page number must be >= 0 and page size must be > 0");
        }
        return pageable;
    }

    @GetMapping
    @Operation(summary = "Get audit logs - Read only")
    public ResponseEntity<PagedModel<EntityModel<AuditLogResponse>>> getLogs(
            @RequestParam(required = false) String tableName,
            @RequestParam(required = false) String operation,
            @PageableDefault(size = 20, sort = "changedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Pageable validatedPageable = validatePageable(pageable);
        PagedResponse<AuditLogResponse> logs =
                auditLogService.getLogs(tableName, operation, validatedPageable);

        var pagedModel = PagedModel.of(
                logs.content().stream().map(this::addLinksToLog).toList(),
                new PagedModel.PageMetadata(
                        logs.size(), logs.page(), logs.totalElements()
                )
        );

        pagedModel.add(linkTo(methodOn(AuditLogController.class)
                .getLogs(tableName, operation, validatedPageable)).withSelfRel());

        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get audit log by ID - Read only")
    public ResponseEntity<EntityModel<AuditLogResponse>> getLog(@PathVariable Long id) {
        return ResponseEntity.ok(addLinksToLog(auditLogService.getLogById(id)));
    }

    private EntityModel<AuditLogResponse> addLinksToLog(AuditLogResponse log) {
        Link selfLink = linkTo(methodOn(AuditLogController.class).getLog(log.id())).withSelfRel();
        Link logsLink = linkTo(methodOn(AuditLogController.class).getLogs(null, null, null)).withRel("logs");
        return EntityModel.of(log, selfLink, logsLink);
    }
}
