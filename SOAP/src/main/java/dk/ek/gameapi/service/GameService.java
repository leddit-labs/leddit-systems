package dk.ek.gameapi.service;

import dk.ek.gameapi.entity.Game;
import dk.ek.gameapi.exception.ResourceNotFoundException;
import dk.ek.gameapi.repository.GameRepository;
import dk.ek.gameapi.dto.GameRequest;
import dk.ek.gameapi.dto.GameResponse;
import dk.ek.gameapi.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameService {

    private final GameRepository gameRepository;

    public PagedResponse<GameResponse> getGames(String search, Pageable pageable) {
        Page<Game> games;
        if (search != null && !search.isBlank()) {
            games = gameRepository.findByNameContainingIgnoreCase(search, pageable);
        } else {
            games = gameRepository.findAll(pageable);
        }
        return PagedResponse.from(games, GameResponse::from);
    }

    public PagedResponse<GameResponse> getAvailableGames(Pageable pageable) {
        return PagedResponse.from(
                gameRepository.findByAvailableTrue(pageable),
                GameResponse::from
        );
    }

    public PagedResponse<GameResponse> getGamesByYearRange(Integer start, Integer end, Pageable pageable) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end years are required");
        }
        if (start > end) {
            throw new IllegalArgumentException("Start year must be less than or equal to end year");
        }
        return PagedResponse.from(
                gameRepository.findByYearRange(start, end, pageable),
                GameResponse::from
        );
    }

    public GameResponse getGameById(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid game ID: " + id);
        }
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game", id));
        return GameResponse.from(game);
    }

    @Transactional
    public GameResponse createGame(GameRequest request, String userId) {
        if (request == null) {
            throw new IllegalArgumentException("Game request cannot be null");
        }
        Game game = new Game();
        updateGameFromRequest(game, request);
        Game saved = gameRepository.save(game);
        return GameResponse.from(saved);
    }

    @Transactional
    public GameResponse updateGame(Integer id, GameRequest request, String userId) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid game ID: " + id);
        }
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game", id));
        updateGameFromRequest(game, request);
        return GameResponse.from(gameRepository.save(game));
    }

    @Transactional
    public void deleteGame(Integer id, String userId) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid game ID: " + id);
        }
        if (!gameRepository.existsById(id)) {
            throw new ResourceNotFoundException("Game", id);
        }
        gameRepository.deleteById(id);
    }

    private void updateGameFromRequest(Game game, GameRequest request) {
        game.setName(request.name());
        game.setSlug(request.slug());
        game.setYearPublished(request.yearPublished());
        game.setBggRating(request.bggRating());
        game.setDifficultyRating(request.difficultyRating());
        game.setDescription(request.description());
        game.setPlayingTime(request.playingTime());
        game.setAvailable(request.available() != null ? request.available() : true);
        game.setMinPlayers(request.minPlayers());
        game.setMaxPlayers(request.maxPlayers());
        game.setMinimumAge(request.minimumAge());
        game.setThumbnail(request.thumbnail());
        game.setImage(request.image());
    }
}
