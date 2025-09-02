package app.newellgames.review.service;

import app.newellgames.game.model.Game;
import app.newellgames.game.service.GameService;
import app.newellgames.review.model.Review;
import app.newellgames.review.repository.ReviewRepository;
import app.newellgames.user.model.User;
import app.newellgames.user.service.UserService;
import app.newellgames.web.dto.AddReviewRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final GameService gameService;

    public ReviewService(ReviewRepository reviewRepository, UserService userService, GameService gameService) {
        this.reviewRepository = reviewRepository;
        this.userService = userService;
        this.gameService = gameService;
    }

    // Creates and saves the review to the DB, assigns it to the User and the Game
    @Transactional
    public void addReview (User user, Game game, AddReviewRequest addReviewRequest) {
        Review review = Review.builder()
                .title(addReviewRequest.getTitle())
                .reviewDescription(addReviewRequest.getDescription())
                .type(addReviewRequest.getType())
                .author(user)
                .game(game)
                .createdOn(LocalDateTime.now())
                .build();

        reviewRepository.save(review);
        userService.addUsersReview(review, user);
        gameService.addGameReview(review, game);
    }
}
