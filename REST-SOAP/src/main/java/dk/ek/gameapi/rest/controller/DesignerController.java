package dk.ek.gameapi.rest.controller;

import dk.ek.gameapi.rest.dto.DesignerRequest;
import dk.ek.gameapi.rest.dto.DesignerResponse;
import dk.ek.gameapi.rest.dto.PagedResponse;
import dk.ek.gameapi.service.DesignerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/designers")
@RequiredArgsConstructor
public class DesignerController {

    private final DesignerService designerService;

    private Pageable validatePageable(Pageable pageable) {
        if (pageable.getPageNumber() < 0 || pageable.getPageSize() <= 0) {
            throw new IllegalArgumentException("Page number must be >= 0 and page size must be > 0");
        }
        return pageable;
    }

    @GetMapping
    @Operation(summary = "Get all designers - Public access")
    public ResponseEntity<PagedModel<EntityModel<DesignerResponse>>> getDesigners(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {

        Pageable validatedPageable = validatePageable(pageable);
        PagedResponse<DesignerResponse> designers = designerService.getDesigners(search, validatedPageable);

        var pagedModel = PagedModel.of(
                designers.content().stream()
                        .map(designer -> addLinksToDesigner(designer, false))
                        .toList(),
                new PagedModel.PageMetadata(
                        designers.size(), designers.page(), designers.totalElements()
                )
        );

        pagedModel.add(linkTo(methodOn(DesignerController.class)
                .getDesigners(search, validatedPageable)).withSelfRel());

        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get designer by ID - Public access")
    public ResponseEntity<EntityModel<DesignerResponse>> getDesigner(
            @PathVariable Integer id,
            Authentication authentication) {

        boolean isAuthenticated = authentication != null && authentication.isAuthenticated();
        return ResponseEntity.ok(addLinksToDesigner(designerService.getDesignerById(id), isAuthenticated));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new designer - Requires login")
    public ResponseEntity<EntityModel<DesignerResponse>> createDesigner(
            @Valid @RequestBody DesignerRequest request) {

        DesignerResponse created = designerService.createDesigner(request);

        return ResponseEntity
                .created(URI.create("/api/v1/designers/" + created.id()))
                .body(addLinksToDesigner(created, true));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update designer - Requires login")
    public ResponseEntity<EntityModel<DesignerResponse>> updateDesigner(
            @PathVariable Integer id,
            @Valid @RequestBody DesignerRequest request) {

        return ResponseEntity.ok(addLinksToDesigner(designerService.updateDesigner(id, request), true));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete designer - Requires login")
    public ResponseEntity<Void> deleteDesigner(@PathVariable Integer id) {
        designerService.deleteDesigner(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<DesignerResponse> addLinksToDesigner(DesignerResponse designer, boolean isAuthenticated) {
        Link selfLink = linkTo(methodOn(DesignerController.class)
                .getDesigner(designer.id(), null)).withSelfRel();

        var model = EntityModel.of(designer, selfLink);

        if (isAuthenticated) {
            Link updateLink = linkTo(methodOn(DesignerController.class)
                    .updateDesigner(designer.id(), null)).withRel("update");
            Link deleteLink = linkTo(methodOn(DesignerController.class)
                    .deleteDesigner(designer.id())).withRel("delete");
            model.add(updateLink, deleteLink);
        }

        Link designersLink = linkTo(methodOn(DesignerController.class)
                .getDesigners(null, null)).withRel("designers");
        model.add(designersLink);

        return model;
    }
}
