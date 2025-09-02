package app.newellgames.web;

import app.newellgames.config.CustomAuthenticationFailureHandler;
import app.newellgames.security.AuthenticationMetadata;
import app.newellgames.user.model.UserRole;
import app.newellgames.user.service.UserService;
import app.newellgames.web.controllers.CartController;
import app.newellgames.web.controllers.ProfileController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.UUID;

import static app.newellgames.TestBuilder.aRandomUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@WebMvcTest(ProfileController.class)
@Import(CustomAuthenticationFailureHandler.class)
public class ProfileControllerApiTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRequestToProfileEndpoint_ShouldReturnProfileView() throws Exception {
        when(userService.getById(any())).thenReturn(aRandomUser());

        UUID userId = UUID.randomUUID();
        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/profile/{id}", userId)
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void getRequestToProfileEditProfileEndpoint_ShouldReturnEditProfileView() throws Exception {
        when(userService.getById(any())).thenReturn(aRandomUser());

        UUID userId = UUID.randomUUID();
        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/profile/{id}/edit-profile", userId)
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("edit-profile"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("editProfileRequest"));
    }

    @Test
    void putRequestToProfileEditProfileEndpoint_HappyPath_ShouldRedirectToProfileView() throws Exception {


        UUID userId = UUID.randomUUID();
        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put("/profile/{id}/edit-profile", userId)
                .formField("username", "123123")
                .formField("email", "123123@123.com")
                .formField("profilePicture", "123123")
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());
        doNothing().when(userService).editProfile(any(), any());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/profile/*"));

        verify(userService, times(1)).editProfile(any(), any());
    }

    @Test
    void putRequestToProfileEditProfileEndpoint_InvalidDataPath_ShouldRedirectToEditProfileView() throws Exception {

        UUID userId = UUID.randomUUID();
        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put("/profile/{id}/edit-profile", userId)
                .formField("username", "1")
                .formField("email", "123123@123com")
                .formField("profilePicture", "123123")
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("edit-profile"))
                .andExpect(model().attributeExists("editProfileRequest"))
                .andExpect(model().attributeExists("errors"));

        verify(userService, times(0)).editProfile(any(), any());
    }
}
