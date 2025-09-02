package app.newellgames;

import app.newellgames.cart.repository.CartRepository;
import app.newellgames.exception.UsernameAlreadyExistException;
import app.newellgames.notification.service.NotificationService;
import app.newellgames.user.model.User;
import app.newellgames.user.repository.UserRepository;
import app.newellgames.user.service.UserService;
import app.newellgames.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class RegisterNewUserITest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void registerNewUser_happyPath() {

        // given
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .password("password")
                .build();

        // when
        User savedUser = userService.register(request);

        // then
        assertThat(userRepository.findByUsername("testuser")).isPresent();
        assertThat(cartRepository.findByUserId(savedUser.getId())).isPresent();

        // verify notification was called
        verify(notificationService, times(1)).saveNotificationPreference(savedUser.getId(), false, null);
    }

    @Test
    void registerNewUser_duplicateUsername_shouldThrowException() {
        // given
        RegisterRequest request1 = new RegisterRequest();
        request1.setUsername("testuser1");
        request1.setPassword("pass");
        userService.register(request1);

        RegisterRequest request2 = new RegisterRequest();
        request2.setUsername("testuser1");
        request2.setPassword("pass");

        // when / then
        assertThatThrownBy(() -> userService.register(request2))
                .isInstanceOf(UsernameAlreadyExistException.class);
    }
}
