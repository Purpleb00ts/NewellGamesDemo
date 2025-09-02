package app.newellgames.game.model;

import app.newellgames.review.model.Review;
import app.newellgames.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table (name = "games")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String publisher;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalDate releaseDate;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Genre genre;

    @ManyToMany (mappedBy = "myGames")
    private List<User> users = new ArrayList<>();

    private int positiveReviews;

    private int negativeReviews;
    //reviews
    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER)
    private List<Review> reviews = new ArrayList<>();

}
