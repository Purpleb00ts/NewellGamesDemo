package app.newellgames.game.repository;

import app.newellgames.game.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GameRepository extends JpaRepository<Game, UUID> {
    List<Game> findAllByOrderByTitleDesc();

    List<Game> findGamesByTitleStartingWithIgnoreCase(String title);
}
