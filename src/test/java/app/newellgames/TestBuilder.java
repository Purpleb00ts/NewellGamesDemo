package app.newellgames;

import app.newellgames.cart.model.Cart;
import app.newellgames.cart.model.CartItem;
import app.newellgames.game.model.Game;
import app.newellgames.game.model.Genre;
import app.newellgames.review.model.Review;
import app.newellgames.review.model.ReviewType;
import app.newellgames.transaction.model.Transaction;
import app.newellgames.transaction.model.TransactionStatus;
import app.newellgames.user.model.User;
import app.newellgames.user.model.UserRole;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@UtilityClass
public class TestBuilder {

    public static User aRandomUser () {

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("randomUser")
                .password("randomPassword")
                .role(UserRole.USER)
                .email(null)
                .updatedOn(LocalDateTime.now())
                .createdOn(LocalDateTime.now())
                .isActive(true)
                .balance(BigDecimal.valueOf(100))
                .profilePicture(null)
                .build();

        Cart cart = Cart.builder()
                .id(UUID.randomUUID())
                .user(user)
                .items(List.of())
                .build();

        cart.setItems(List.of(new CartItem(UUID.randomUUID(), cart, aRandomGame(), BigDecimal.valueOf(100))));

        user.setCart(cart);

        return user;
    }

    public static Game aRandomGame() {

        return Game.builder()
                .genre(Genre.MMO)
                .price(BigDecimal.valueOf(100))
                .description("randomDescription")
                .title("randomTitle")
                .imageUrl("randomImageUrl")
                .negativeReviews(1)
                .positiveReviews(1)
                .publisher("randomPublisher")
                .releaseDate(LocalDate.now())
                .build();
    }

    public static Review aRandomReview() {
        return Review.builder()
                .id(UUID.randomUUID())
                .type(ReviewType.POSITIVE)
                .title("randomTitle")
                .reviewDescription("randomDescription")
                .createdOn(LocalDateTime.now())
                .build();
    }

    public static Transaction aRandomTransaction() {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .amount(BigDecimal.valueOf(100))
                .completedOn(LocalDateTime.now())
                .createdOn(LocalDateTime.now())
                .status(TransactionStatus.SUCCESSFUL)
                .build();
    }
}
