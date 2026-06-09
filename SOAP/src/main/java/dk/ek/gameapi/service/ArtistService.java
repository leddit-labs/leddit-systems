package dk.ek.gameapi.service;

import dk.ek.gameapi.entity.Artist;
import dk.ek.gameapi.exception.ResourceNotFoundException;
import dk.ek.gameapi.repository.ArtistRepository;
import dk.ek.gameapi.dto.ArtistRequest;
import dk.ek.gameapi.dto.ArtistResponse;
import dk.ek.gameapi.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArtistService {

    private final ArtistRepository artistRepository;

    public PagedResponse<ArtistResponse> getArtists(String search, Pageable pageable) {
        Page<Artist> artists;
        if (search != null && !search.isBlank()) {
            artists = artistRepository.findByNameContainingIgnoreCase(search, pageable);
        } else {
            artists = artistRepository.findAll(pageable);
        }
        return PagedResponse.from(artists, ArtistResponse::from);
    }

    public ArtistResponse getArtistById(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid artist ID: " + id);
        }
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artist", id));
        return ArtistResponse.from(artist);
    }

    @Transactional
    public ArtistResponse createArtist(ArtistRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Artist request cannot be null");
        }
        Artist artist = new Artist();
        updateArtistFromRequest(artist, request);
        return ArtistResponse.from(artistRepository.save(artist));
    }

    @Transactional
    public ArtistResponse updateArtist(Integer id, ArtistRequest request) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid artist ID: " + id);
        }
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artist", id));
        updateArtistFromRequest(artist, request);
        return ArtistResponse.from(artistRepository.save(artist));
    }

    @Transactional
    public void deleteArtist(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid artist ID: " + id);
        }
        if (!artistRepository.existsById(id)) {
            throw new ResourceNotFoundException("Artist", id);
        }
        artistRepository.deleteById(id);
    }

    private void updateArtistFromRequest(Artist artist, ArtistRequest request) {
        artist.setName(request.name());
        artist.setDob(request.dob());
    }
}
