package dk.ek.gameapi.rest.dto;

import dk.ek.gameapi.entity.Game;

public record GameResponse(
        Integer id,
        String name,
        String slug,
        Integer yearPublished,
        Double bggRating,
        Double difficultyRating,
        String description,
        Integer playingTime,
        Boolean available,
        Integer minPlayers,
        Integer maxPlayers,
        Integer minimumAge,
        String thumbnail,
        String image
) {
    public static GameResponse from(Game g) {
        return new GameResponse(
                g.getId(), g.getName(), g.getSlug(), g.getYearPublished(),
                g.getBggRating(), g.getDifficultyRating(), g.getDescription(),
                g.getPlayingTime(), g.getAvailable(), g.getMinPlayers(),
                g.getMaxPlayers(), g.getMinimumAge(), g.getThumbnail(), g.getImage()
        );
    }
}
