package dk.ek.gameapi.rest.controller;

import dk.ek.gameapi.rest.dto.ArtistRequest;
import dk.ek.gameapi.rest.dto.ArtistResponse;
import dk.ek.gameapi.rest.dto.PagedResponse;
import dk.ek.gameapi.service.ArtistService;
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
@RequestMapping("/api/v1/artists")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;

    private Pageable validatePageable(Pageable pageable) {
        if (pageable.getPageNumber() < 0 || pageable.getPageSize() <= 0) {
            throw new IllegalArgumentException("Page number must be >= 0 and page size must be > 0");
        }
        return pageable;
    }

    @GetMapping
    @Operation(summary = "Get all artists - Public access")
    public ResponseEntity<PagedModel<EntityModel<ArtistResponse>>> getArtists(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {

        Pageable validatedPageable = validatePageable(pageable);
        PagedResponse<ArtistResponse> artists = artistService.getArtists(search, validatedPageable);

        var pagedModel = PagedModel.of(
                artists.content().stream()
                        .map(artist -> addLinksToArtist(artist, false))
                        .toList(),
                new PagedModel.PageMetadata(
                        artists.size(), artists.page(), artists.totalElements()
                )
        );

        pagedModel.add(linkTo(methodOn(ArtistController.class)
                .getArtists(search, validatedPageable)).withSelfRel());

        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get artist by ID - Public access")
    public ResponseEntity<EntityModel<ArtistResponse>> getArtist(
            @PathVariable Integer id,
            Authentication authentication) {

        boolean isAuthenticated = authentication != null && authentication.isAuthenticated();
        return ResponseEntity.ok(addLinksToArtist(artistService.getArtistById(id), isAuthenticated));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new artist - Requires login")
    public ResponseEntity<EntityModel<ArtistResponse>> createArtist(
            @Valid @RequestBody ArtistRequest request) {

        ArtistResponse created = artistService.createArtist(request);

        return ResponseEntity
                .created(URI.create("/api/v1/artists/" + created.id()))
                .body(addLinksToArtist(created, true));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update artist - Requires login")
    public ResponseEntity<EntityModel<ArtistResponse>> updateArtist(
            @PathVariable Integer id,
            @Valid @RequestBody ArtistRequest request) {

        return ResponseEntity.ok(addLinksToArtist(artistService.updateArtist(id, request), true));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete artist - Requires login")
    public ResponseEntity<Void> deleteArtist(@PathVariable Integer id) {
        artistService.deleteArtist(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<ArtistResponse> addLinksToArtist(ArtistResponse artist, boolean isAuthenticated) {
        Link selfLink = linkTo(methodOn(ArtistController.class)
                .getArtist(artist.id(), null)).withSelfRel();

        var model = EntityModel.of(artist, selfLink);

        if (isAuthenticated) {
            Link updateLink = linkTo(methodOn(ArtistController.class)
                    .updateArtist(artist.id(), null)).withRel("update");
            Link deleteLink = linkTo(methodOn(ArtistController.class)
                    .deleteArtist(artist.id())).withRel("delete");
            model.add(updateLink, deleteLink);
        }

        Link artistsLink = linkTo(methodOn(ArtistController.class)
                .getArtists(null, null)).withRel("artists");
        model.add(artistsLink);

        return model;
    }
}
