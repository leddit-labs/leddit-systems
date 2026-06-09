package dk.ek.gameapi.rest.dto;

import dk.ek.gameapi.entity.Artist;

import java.time.LocalDate;

public record ArtistResponse(
        Integer id,
        String name,
        LocalDate dob
) {
    public static ArtistResponse from(Artist artist) {
        return new ArtistResponse(
                artist.getId(),
                artist.getName(),
                artist.getDob()
        );
    }
}
