package dk.ek.gameapi.service;

import dk.ek.gameapi.entity.Game;
import dk.ek.gameapi.exception.ResourceNotFoundException;
import dk.ek.gameapi.repository.GameRepository;
import dk.ek.gameapi.rest.dto.GameResponse;
import dk.ek.gameapi.rest.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameService {

    private final GameRepository gameRepository;

    public PagedResponse<GameResponse> getGames(Pageable pageable) {
        return PagedResponse.from(gameRepository.findAll(pageable), GameResponse::from);
    }

    public GameResponse getGameById(Integer id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game", id));
        return GameResponse.from(game);
    }
}
