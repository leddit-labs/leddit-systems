package dk.ek.gameapi.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record GameRequest(
        @NotBlank(message = "Name is required")
        String name,

        String slug,

        @PositiveOrZero
        Integer yearPublished,

        @PositiveOrZero
        Double bggRating,

        @PositiveOrZero
        Double difficultyRating,

        String description,

        @Positive
        Integer playingTime,

        Boolean available,

        @Positive
        Integer minPlayers,

        @Positive
        Integer maxPlayers,

        @Positive
        Integer minimumAge,

        String thumbnail,

        String image
) {}