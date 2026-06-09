package dk.ek.gameapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "game")
@Getter
@Setter
@NoArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String name;
    private String slug;
    @Column(name = "year_published", columnDefinition = "YEAR")
    private Integer yearPublished;
    private Double bggRating;
    private Double difficultyRating;
    @Column(columnDefinition = "TEXT")
    private String description;
    private Integer playingTime;
    private Boolean available;
    private Integer minPlayers;
    private Integer maxPlayers;
    private Integer minimumAge;
    @Column(columnDefinition = "TEXT")
    private String thumbnail;
    @Column(columnDefinition = "TEXT")
    private String image;
}
