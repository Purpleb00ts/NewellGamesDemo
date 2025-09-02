package app.newellgames;

import app.newellgames.cart.model.CartItem;
import app.newellgames.cart.repository.CartItemRepository;
import app.newellgames.cart.repository.CartRepository;
import app.newellgames.cart.service.CartService;
import app.newellgames.game.model.Game;
import app.newellgames.game.repository.GameRepository;
import app.newellgames.notification.service.NotificationService;
import app.newellgames.transaction.model.Transaction;
import app.newellgames.transaction.model.TransactionStatus;
import app.newellgames.transaction.model.TransactionType;
import app.newellgames.transaction.repository.TransactionRepository;
import app.newellgames.transaction.service.TransactionService;
import app.newellgames.user.model.User;
import app.newellgames.user.repository.UserRepository;
import app.newellgames.user.service.UserService;
import app.newellgames.web.dto.RegisterRequest;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static app.newellgames.TestBuilder.aRandomGame;
import static app.newellgames.TestBuilder.aRandomUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class PurchaseCartItemsITest {

    @Autowired
    private UserService userService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    @Transactional
    void purchaseCartItems_happyPath() {

        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("testuser")
                .password("password")
                .build();

        User user = userService.register(registerRequest); // user is managed
        Game game = gameRepository.save(aRandomGame());    // game is managed


        CartItem cartItem = CartItem.builder()
                .game(game)
                .price(BigDecimal.TEN)
                .cart(user.getCart())
                .build();

        user.getCart().setItems(new ArrayList<>());
        user.getCart().getItems().add(cartItem);

        user.setBalance(BigDecimal.valueOf(101));
        user.setMyGames(new ArrayList<>());

        doNothing().when(notificationService).sendNotification(any(), any(), any());

        userService.purchaseCartItems(user);

        assertEquals(BigDecimal.valueOf(91), user.getBalance());
        assertTrue(user.getMyGames().contains(game));
        assertTrue(user.getCart().getItems().isEmpty());

        Optional <Transaction> tx = transactionRepository.findByOwnerUsernameContainingIgnoreCaseOrderByCreatedOnDesc(user.getUsername()).stream().findFirst();
        assertTrue(tx.isPresent());
        assertEquals(BigDecimal.valueOf(10), tx.get().getAmount());
        assertSame(TransactionStatus.SUCCESSFUL, tx.get().getStatus());
        assertSame(TransactionType.PURCHASE, tx.get().getType());

        verify(notificationService, times(1)).sendNotification(any(), any(), any());
    }

    @Test
    @Transactional
    void purchaseCartItems_InsufficientFunds() {

        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("testuser")
                .password("password")
                .build();

        User user = userService.register(registerRequest);
        Game game = gameRepository.save(aRandomGame());

        CartItem cartItem = CartItem.builder()
                .game(game)
                .price(BigDecimal.TEN)
                .cart(user.getCart())
                .build();

        user.getCart().setItems(new ArrayList<>());
        user.getCart().getItems().add(cartItem);

        user.setBalance(BigDecimal.valueOf(1));
        user.setMyGames(new ArrayList<>());

        doNothing().when(notificationService).sendNotification(any(), any(), any());

        userService.purchaseCartItems(user);

        Optional <Transaction> tx = transactionRepository.findByOwnerUsernameContainingIgnoreCaseOrderByCreatedOnDesc(user.getUsername()).stream().findFirst();
        assertTrue(tx.isPresent());
        assertEquals(BigDecimal.valueOf(10), tx.get().getAmount());
        assertSame(TransactionStatus.FAILED, tx.get().getStatus());
        assertSame(TransactionType.PURCHASE, tx.get().getType());

        assertEquals(BigDecimal.ONE , user.getBalance());
        assertFalse(user.getMyGames().contains(game));
        assertFalse(user.getCart().getItems().isEmpty());

        verify(notificationService, times(0)).sendNotification(any(), any(), any());
    }
}
