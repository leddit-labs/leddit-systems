package dk.ek.gameapi.rest.controller;

import dk.ek.gameapi.rest.dto.GameResponse;
import dk.ek.gameapi.rest.dto.PagedResponse;
import dk.ek.gameapi.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    // GET /api/v1/games?page=0&size=20&sort=name,asc
    // Spring binds the Pageable from query params automatically.
    // Default sort by id keeps paging deterministic (MySQL order isn't guaranteed otherwise).
    @GetMapping
    public ResponseEntity<PagedResponse<GameResponse>> getGames(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(gameService.getGames(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameResponse> getGame(@PathVariable Long id) {
        return ResponseEntity.ok(gameService.getGameById(id));
    }
}
