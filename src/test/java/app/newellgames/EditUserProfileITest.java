package app.newellgames;

import app.newellgames.exception.EmailAlreadyExistException;
import app.newellgames.exception.UsernameAlreadyExistException;
import app.newellgames.notification.client.dto.NotificationPreference;
import app.newellgames.notification.service.NotificationService;
import app.newellgames.user.model.User;
import app.newellgames.user.repository.UserRepository;
import app.newellgames.user.service.UserService;
import app.newellgames.web.dto.EditProfileRequest;
import app.newellgames.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class EditUserProfileITest {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @MockitoBean
    NotificationService notificationService;

    @Test
    void editUserProfile_WhenUserDoesNotHaveEmail() {

        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("testuser")
                .password("password")
                .build();

        User userToEdit = userService.register(registerRequest);

        EditProfileRequest editProfileRequest = EditProfileRequest.builder()
                .username("Kaloyan")
                .email("kalchev1998@gmail.com")
                .profilePicture("cool picture")
                .build();


        when(notificationService.getNotificationPreference(any())).thenReturn(new NotificationPreference());
        doNothing().when(notificationService).saveNotificationPreference(UUID.randomUUID(), false, userToEdit.getEmail());
        doNothing().when(notificationService).sendNotification(any(), any(), any());

        userService.editProfile(userToEdit, editProfileRequest);
        User user = userRepository.findById(userToEdit.getId()).orElseThrow();


        assertEquals(editProfileRequest.getUsername(), user.getUsername());
        assertEquals(editProfileRequest.getEmail(), user.getEmail());
        assertEquals(editProfileRequest.getProfilePicture(), user.getProfilePicture());

        Optional<User> userWithNewUsername = userRepository.findByUsername(editProfileRequest.getUsername());
        assertTrue(userWithNewUsername.isPresent());
        assertEquals(userWithNewUsername.get().getUsername(), user.getUsername());

        Optional<User> userWithNewEmail = userRepository.findByEmail(editProfileRequest.getEmail());
        assertTrue(userWithNewEmail.isPresent());
        assertEquals(userWithNewEmail.get().getEmail(), user.getEmail());
    }

    @Test
    void editUserProfile_WhenUserTriesToUseAlreadyUsedEmail() {

        RegisterRequest registerRequestInitialUser = RegisterRequest.builder()
                .username("testuser1")
                .password("password")
                .build();

        RegisterRequest registerRequestUserWithSameEmail = RegisterRequest.builder()
                .username("testuser2")
                .password("password")
                .build();

        User userToEdit = userService.register(registerRequestInitialUser);
        User userWithSameEmail = userService.register(registerRequestUserWithSameEmail);

        EditProfileRequest editProfileRequestForUserToEdit = EditProfileRequest.builder()
                .username("testuser1")
                .email("kalchev1998@gmail.com")
                .profilePicture("cool picture")
                .build();

        EditProfileRequest editProfileRequestForUserWithSameEmail = EditProfileRequest.builder()
                .username("testuser2")
                .email("kalchev1998@gmail.com")
                .profilePicture("cool picture")
                .build();


        when(notificationService.getNotificationPreference(any())).thenReturn(new NotificationPreference());
        doNothing().when(notificationService).saveNotificationPreference(UUID.randomUUID(), false, userToEdit.getEmail());
        doNothing().when(notificationService).sendNotification(any(), any(), any());

        userService.editProfile(userWithSameEmail, editProfileRequestForUserWithSameEmail);

        userToEdit = userRepository.findById(userToEdit.getId()).orElseThrow();
        userWithSameEmail = userRepository.findByEmail(userWithSameEmail.getEmail()).orElseThrow();

        User finalUserToEdit = userToEdit;

        assertThrows(EmailAlreadyExistException.class, () -> userService.editProfile(finalUserToEdit, editProfileRequestForUserToEdit));
    }

    @Test
    void editUserProfile_WhenUserTriesToUseAlreadyUsedUsername() {

        RegisterRequest registerRequestInitialUser = RegisterRequest.builder()
                .username("testuser1")
                .password("password")
                .build();

        RegisterRequest registerRequestUserWithSameEmail = RegisterRequest.builder()
                .username("testuser2")
                .password("password")
                .build();

        User userToEdit = userService.register(registerRequestInitialUser);
        userService.register(registerRequestUserWithSameEmail);

        EditProfileRequest editProfileRequestForUserToEdit = EditProfileRequest.builder()
                .username("testuser2")
                .email("kalchev1998@gmail.com")
                .profilePicture("cool picture")
                .build();


        when(notificationService.getNotificationPreference(any())).thenReturn(new NotificationPreference());
        doNothing().when(notificationService).saveNotificationPreference(UUID.randomUUID(), false, userToEdit.getEmail());
        doNothing().when(notificationService).sendNotification(any(), any(), any());

        userToEdit = userRepository.findById(userToEdit.getId()).orElseThrow();

        User finalUserToEdit = userToEdit;

        assertThrows(UsernameAlreadyExistException.class, () -> userService.editProfile(finalUserToEdit, editProfileRequestForUserToEdit));
    }

}
