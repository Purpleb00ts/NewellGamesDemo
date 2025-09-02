package app.newellgames.game.service;

import app.newellgames.exception.DomainException;
import app.newellgames.game.model.Game;
import app.newellgames.game.repository.GameRepository;
import app.newellgames.review.model.Review;
import app.newellgames.review.model.ReviewType;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GameService {

    private final GameRepository gameRepository;

    @Autowired
    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public List<Game> getAllGames() {
        return gameRepository.findAllByOrderByTitleDesc();
    }


    // Gets game by ID
    public Game getById(UUID id) {
        return gameRepository.findById(id).orElseThrow(() -> new DomainException("Game with id [%s] does not exist.".formatted(id)));
    }

    // Assigns a review to the game
    @Transactional
    public void addGameReview (Review review, Game game) {
        if(review.getType() == ReviewType.POSITIVE)
        {
            game.setPositiveReviews(game.getPositiveReviews() + 1);
        } else {
            game.setNegativeReviews(game.getNegativeReviews() + 1);
        }
        game.getReviews().add(review);
        gameRepository.save(game);
    }

    // Gets the game by Title for Shop search functionality
    public List<Game> findGamesByTitle(String title) {
        return gameRepository.findGamesByTitleStartingWithIgnoreCase(title);
    }
}
