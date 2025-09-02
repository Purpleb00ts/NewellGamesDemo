package app.newellgames.user;


import app.newellgames.cart.service.CartService;
import app.newellgames.game.model.Game;
import app.newellgames.notification.service.NotificationService;
import app.newellgames.transaction.model.Transaction;
import app.newellgames.transaction.service.TransactionService;
import app.newellgames.user.model.User;
import app.newellgames.user.model.UserRole;
import app.newellgames.user.repository.UserRepository;
import app.newellgames.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static app.newellgames.TestBuilder.aRandomGame;
import static app.newellgames.TestBuilder.aRandomTransaction;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceUTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private CartService cartService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @ParameterizedTest
    @MethodSource("userRolesArguments")
    void whenChangeUserRoleToAdmin_theCorrectRoleIsAssigned(UserRole currentUserRole, UserRole expectedUserRole) {

        // Given
        User user = User.builder()
                .role(currentUserRole)
                .build();

        // When
        userService.switchUserRoleToAdmin(user);

        // Then
        assertEquals(expectedUserRole, user.getRole());
    }

    private static Stream<Arguments> userRolesArguments() {

        return Stream.of(
                Arguments.of(UserRole.USER, UserRole.ADMIN)
        );
    }

    @Test
    void whenChangeUserRoleToUser_theCorrectRoleIsAssigned() {

        // Given
        User user = User.builder()
                .role(UserRole.ADMIN)
                .build();

        // When
        userService.switchUserRoleToUser(user);

        // Then
        assertEquals(UserRole.USER, user.getRole());
    }

    @Test
    void whenChangeUserStatusToActive_theCorrectStatusIsAssigned() {

        // Given
        User user = User.builder()
                .isActive(false)
                .build();

        // When
        userService.switchUserStatusToActive(user);

        // Then
        assertTrue(user.isActive());
    }

    @Test
    void whenChangeUserStatusToInActive_theCorrectStatusIsAssigned() {

        // Given
        User user = User.builder()
                .isActive(true)
                .build();

        // When
        userService.switchUserStatusToInActive(user);

        // Then
        assertFalse(user.isActive());
    }

    @Test
    void whenGetOwnedGameIds_thenUsersGameListIsReturned() {
        Game game = aRandomGame();
        game.setId(UUID.randomUUID());

        User  user = User.builder()
                .id(UUID.randomUUID())
                .myGames(List.of(game))
                .build();

        Set<UUID> userGames = userService.getOwnedGameIds(user);

        assertThat(userGames).hasSize(1);
        assertTrue(userGames.contains(game.getId()));
    }

    @Test
    void whenGetTransactions_thenUsersTransactionListIsReturned() {
        Transaction transaction = aRandomTransaction();
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();

        when(transactionService.getAllTransactionsByUser(user)).thenReturn(List.of(transaction));

        List<Transaction> usersTransactions = userService.getTransactions(user);

        assertThat(usersTransactions).hasSize(1);
        assertTrue(usersTransactions.contains(transaction));
    }
}
