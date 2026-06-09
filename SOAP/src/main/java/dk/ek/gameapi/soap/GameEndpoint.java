
package dk.ek.gameapi.soap;

import dk.ek.gameapi.service.GameService;
import dk.ek.gameapi.soap.gen.*;
import dk.ek.gameapi.dto.GameRequest;
import dk.ek.gameapi.dto.GameResponse;
import dk.ek.gameapi.dto.PagedResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
@RequiredArgsConstructor
public class GameEndpoint {

    private static final String NS = "http://gameapi.ek.dk/soap";
    private static final String SOAP_USER = "soap-client";

    private final GameService gameService;

    // READ 1
    @PayloadRoot(namespace = NS, localPart = "getGameByIdRequest")
    @ResponsePayload
    public GetGameByIdResponse getGameById(@RequestPayload GetGameByIdRequest request) {
        GetGameByIdResponse response = new GetGameByIdResponse();
        response.setGame(toSoap(gameService.getGameById(request.getId())));
        return response;
    }

    // READ 2
    @PayloadRoot(namespace = NS, localPart = "getAllGamesRequest")
    @ResponsePayload
    public GetAllGamesResponse getAllGames(@RequestPayload GetAllGamesRequest request) {
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("page must be >= 0 and size must be > 0");
        }
        PagedResponse<GameResponse> games =
                gameService.getGames(null, PageRequest.of(page, size, Sort.by("id")));

        GetAllGamesResponse response = new GetAllGamesResponse();
        games.content().forEach(g -> response.getGame().add(toSoap(g)));
        response.setTotalElements(games.totalElements());
        response.setTotalPages(games.totalPages());
        return response;
    }

    // WRITE 1
    @PayloadRoot(namespace = NS, localPart = "createGameRequest")
    @ResponsePayload
    public CreateGameResponse createGame(@RequestPayload CreateGameRequest request) {
        CreateGameResponse response = new CreateGameResponse();
        response.setGame(toSoap(gameService.createGame(toRequest(request.getGame()), SOAP_USER)));
        return response;
    }

    // WRITE 2
    @PayloadRoot(namespace = NS, localPart = "updateGameRequest")
    @ResponsePayload
    public UpdateGameResponse updateGame(@RequestPayload UpdateGameRequest request) {
        UpdateGameResponse response = new UpdateGameResponse();
        response.setGame(toSoap(gameService.updateGame(request.getId(), toRequest(request.getGame()), SOAP_USER)));
        return response;
    }

    // WRITE 3
    @PayloadRoot(namespace = NS, localPart = "deleteGameRequest")
    @ResponsePayload
    public DeleteGameResponse deleteGame(@RequestPayload DeleteGameRequest request) {
        gameService.deleteGame(request.getId(), SOAP_USER);
        DeleteGameResponse response = new DeleteGameResponse();
        response.setDeleted(true);
        response.setMessage("Game with id " + request.getId() + " was deleted");
        return response;
    }

    // mapping between the SOAP types and the service DTOs
    private Game toSoap(GameResponse r) {
        Game g = new Game();
        g.setId(r.id());
        g.setName(r.name());
        g.setSlug(r.slug());
        g.setYearPublished(r.yearPublished());
        g.setBggRating(r.bggRating());
        g.setDifficultyRating(r.difficultyRating());
        g.setDescription(r.description());
        g.setPlayingTime(r.playingTime());
        g.setAvailable(r.available());
        g.setMinPlayers(r.minPlayers());
        g.setMaxPlayers(r.maxPlayers());
        g.setMinimumAge(r.minimumAge());
        g.setThumbnail(r.thumbnail());
        g.setImage(r.image());
        return g;
    }

    private GameRequest toRequest(Game g) {
        if (g == null || g.getName() == null) {
            throw new IllegalArgumentException("name is required");
        }
        return new GameRequest(
                g.getName(), g.getSlug(), g.getYearPublished(), g.getBggRating(),
                g.getDifficultyRating(), g.getDescription(), g.getPlayingTime(),
                g.isAvailable(), g.getMinPlayers(), g.getMaxPlayers(),
                g.getMinimumAge(), g.getThumbnail(), g.getImage()
        );
    }
}
