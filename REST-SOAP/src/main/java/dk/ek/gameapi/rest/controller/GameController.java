package dk.ek.gameapi.rest.controller;

import dk.ek.gameapi.rest.dto.GameRequest;
import dk.ek.gameapi.rest.dto.GameResponse;
import dk.ek.gameapi.rest.dto.PagedResponse;
import dk.ek.gameapi.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    private Pageable validatePageable(Pageable pageable) {
        if (pageable.getPageNumber() < 0 || pageable.getPageSize() <= 0) {
            throw new IllegalArgumentException("Page number must be >= 0 and page size must be > 0");
        }
        return pageable;
    }

    @GetMapping
    @Operation(summary = "Get all games - Public access")
    public ResponseEntity<PagedModel<EntityModel<GameResponse>>> getGames(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {

        Pageable validatedPageable = validatePageable(pageable);
        PagedResponse<GameResponse> games = gameService.getGames(search, validatedPageable);

        var pagedModel = PagedModel.of(
                games.content().stream()
                        .map(game -> addLinksToGame(game, false))
                        .toList(),
                new PagedModel.PageMetadata(
                        games.size(), games.page(), games.totalElements()
                )
        );

        pagedModel.add(linkTo(methodOn(GameController.class)
                .getGames(search, validatedPageable)).withSelfRel());

        return ResponseEntity.ok(pagedModel);
    }

    // GET by ID - Public (no auth required)
    @GetMapping("/{id}")
    @Operation(summary = "Get game by ID - Public access")
    public ResponseEntity<EntityModel<GameResponse>> getGame(
            @PathVariable Integer id,
            Authentication authentication) {

        boolean isAuthenticated = authentication != null && authentication.isAuthenticated();
        return ResponseEntity.ok(addLinksToGame(gameService.getGameById(id), isAuthenticated));
    }

    // GET available games - Public
    @GetMapping("/available")
    @Operation(summary = "Get available games - Public access")
    public ResponseEntity<PagedModel<EntityModel<GameResponse>>> getAvailableGames(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {

        PagedResponse<GameResponse> games = gameService.getAvailableGames(pageable);

        var pagedModel = PagedModel.of(
                games.content().stream()
                        .map(game -> addLinksToGame(game, false))
                        .toList(),
                new PagedModel.PageMetadata(
                        games.size(), games.page(), games.totalElements()
                )
        );

        pagedModel.add(linkTo(methodOn(GameController.class)
                .getAvailableGames(pageable)).withSelfRel());

        return ResponseEntity.ok(pagedModel);
    }

    // POST - Requires authentication
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new game - Requires login")
    public ResponseEntity<EntityModel<GameResponse>> createGame(
            @Valid @RequestBody GameRequest request,
            Authentication authentication) {

        String userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            userId = jwt.getClaimAsString("sub");
        }

        GameResponse created = gameService.createGame(request, userId);

        return ResponseEntity
                .created(URI.create("/api/v1/games/" + created.id()))
                .body(addLinksToGame(created, true));
    }

    // PUT - Requires authentication
    @PutMapping("/{id}")
    @Operation(summary = "Update game - Requires login")
    public ResponseEntity<EntityModel<GameResponse>> updateGame(
            @PathVariable Integer id,
            @Valid @RequestBody GameRequest request,
            Authentication authentication) {

        String userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            userId = jwt.getClaimAsString("sub");
        }

        return ResponseEntity.ok(addLinksToGame(gameService.updateGame(id, request, userId), true));
    }

    // DELETE - Requires authentication
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete game - Requires login")
    public ResponseEntity<Void> deleteGame(
            @PathVariable Integer id,
            Authentication authentication) {

        String userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            userId = jwt.getClaimAsString("sub");
        }

        gameService.deleteGame(id, userId);
        return ResponseEntity.noContent().build();
    }

    // Helper method to add HATEOAS links based on authentication status
    private EntityModel<GameResponse> addLinksToGame(GameResponse game, boolean isAuthenticated) {
        Link selfLink = linkTo(methodOn(GameController.class)
                .getGame(game.id(), null)).withSelfRel();

        var model = EntityModel.of(game, selfLink);

        // Only show write links if user is authenticated
        if (isAuthenticated) {
            Link updateLink = linkTo(methodOn(GameController.class)
                    .updateGame(game.id(), null, null)).withRel("update");
            Link deleteLink = linkTo(methodOn(GameController.class)
                    .deleteGame(game.id(), null)).withRel("delete");
            model.add(updateLink, deleteLink);
        }

        Link gamesLink = linkTo(methodOn(GameController.class)
                .getGames(null, null)).withRel("games");
        model.add(gamesLink);

        return model;
    }
}