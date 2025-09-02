package app.newellgames.bootstrap;


import app.newellgames.cart.model.Cart;
import app.newellgames.game.model.Game;
import app.newellgames.game.model.Genre;
import app.newellgames.game.repository.GameRepository;
import app.newellgames.notification.service.NotificationService;
import app.newellgames.user.model.User;
import app.newellgames.user.model.UserRole;
import app.newellgames.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(NotificationService notificationService, UserRepository userRepository,
                           GameRepository gameRepository,
                           PasswordEncoder passwordEncoder) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initUsers();
        initGames();
    }

    private void initUsers() {
        if (userRepository.count() == 0) {

            User admin = User.builder()
                    .username("admin")
                    .email("admin@newellgames.com")
                    .password(passwordEncoder.encode("admin123"))
                    .balance(BigDecimal.valueOf(500))
                    .createdOn(LocalDateTime.now())
                    .updatedOn(LocalDateTime.now())
                    .role(UserRole.ADMIN)
                    .isActive(true)
                    .build();

            Cart forAdmin = Cart.builder()
                    .user(admin)
                    .build();



            admin.setCart(forAdmin);

            User user = User.builder()
                    .username("john")
                    .email("john@example.com")
                    .password(passwordEncoder.encode("password"))
                    .balance(BigDecimal.valueOf(500))
                    .createdOn(LocalDateTime.now())
                    .updatedOn(LocalDateTime.now())
                    .role(UserRole.USER)
                    .isActive(true)
                    .build();

            Cart forUser = Cart.builder()
                    .user(user)
                    .build();

            user.setCart(forUser);

            userRepository.saveAll(List.of(admin, user));
            notificationService.saveNotificationPreference(admin.getId(), false, admin.getEmail());
            notificationService.saveNotificationPreference(user.getId(), false, user.getEmail());
            System.out.println("✅ Demo users inserted.");
        }
    }

    private void initGames() {
        if (gameRepository.count() == 0) {
            Game g1 = Game.builder()
                    .title("Half-Life 3")
                    .publisher("Some publisher")
                    .description("The game that never was.")
                    .price(BigDecimal.valueOf(59.99))
                    .releaseDate(LocalDate.now())
                    .imageUrl("Cool Picture")
                    .genre(Genre.Action)
                    .negativeReviews(0)
                    .positiveReviews(0)
                    .build();

            Game g2 = Game.builder()
                    .title("Portal 3")
                    .publisher("Some publisher")
                    .description("Puzzle platform with portals.")
                    .price(BigDecimal.valueOf(39.99))
                    .releaseDate(LocalDate.now())
                    .imageUrl("Cool Picture")
                    .genre(Genre.Action)
                    .negativeReviews(0)
                    .positiveReviews(0)
                    .build();

            Game g3 = Game.builder()
                    .title("Dota 3")
                    .publisher("Some publisher")
                    .description("MOBA sequel.")
                    .price(BigDecimal.valueOf(29.99))
                    .releaseDate(LocalDate.now())
                    .imageUrl("Cool Picture")
                    .genre(Genre.Action)
                    .negativeReviews(0)
                    .positiveReviews(0)
                    .build();


            gameRepository.saveAll(List.of(g1, g2, g3));
            System.out.println("✅ Demo games inserted.");
        }
    }
}