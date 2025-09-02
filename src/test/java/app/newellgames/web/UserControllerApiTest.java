package app.newellgames.web;


import app.newellgames.config.CustomAuthenticationFailureHandler;
import app.newellgames.security.AuthenticationMetadata;
import app.newellgames.user.model.UserRole;
import app.newellgames.user.service.UserService;
import app.newellgames.web.controllers.TransactionController;
import app.newellgames.web.controllers.UserController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.UUID;

import static app.newellgames.TestBuilder.aRandomTransaction;
import static app.newellgames.TestBuilder.aRandomUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@WebMvcTest(UserController.class)
@Import(CustomAuthenticationFailureHandler.class)
public class UserControllerApiTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRequestToAdminUsers_WithAuthorization_ShouldReturnUsersView () throws Exception {
        UUID userId = UUID.randomUUID();

        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.ADMIN, true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/admin/users")
                .with(user(principal));

        when(userService.getById(any())).thenReturn(aRandomUser());
        when(userService.getAllUsers()).thenReturn(List.of(aRandomUser()));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("users"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("users"));
    }

    @Test
    void getRequestToAdminUsers_WithoutAuthorization_ShouldReturnNotFoundView () throws Exception {
        UUID userId = UUID.randomUUID();

        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/admin/users")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().is4xxClientError())
                .andExpect(view().name("not-found"));
    }

    @Test
    void getRequestToAdminUsers_WithAuthorizationAndQueryParameter_ShouldReturnUsersView () throws Exception {
        UUID userId = UUID.randomUUID();

        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.ADMIN, true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/admin/users").param("query", "")
                .with(user(principal));

        when(userService.getById(any())).thenReturn(aRandomUser());
        when(userService.getUserByUsernameOrIdOrEmail(any())).thenReturn(List.of(aRandomUser()));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("users"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("query"));
    }

    @Test
    void postRequestToAdminUsersSwitchRoleToUser_WithAuthorization_ShouldRedirectToUsersView () throws Exception {
        UUID userId = UUID.randomUUID();

        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.ADMIN, true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/admin/users/switch-role/{id}", userId)
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());
        doNothing().when(userService).switchUserRoleToUser(any());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userService, times(0)).switchUserRoleToAdmin(any());
    }

    @Test
    void postRequestToAdminUsersSwitchRoleToAdmin_WithAuthorization_ShouldRedirectToUsersView () throws Exception {
        UUID userId = UUID.randomUUID();

        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.ADMIN, true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/admin/users/switch-role/{id}", userId).param("isAdmin", "on")
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());
        doNothing().when(userService).switchUserRoleToAdmin(any());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userService, times(0)).switchUserRoleToUser(any());
    }

    @Test
    void postRequestToAdminUsersSwitchRoleToAdmin_WithoutAuthorization_ShouldShowNotFoundView () throws Exception {
        UUID userId = UUID.randomUUID();

        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/admin/users/switch-role/{id}", userId)
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is4xxClientError())
                .andExpect(view().name("not-found"));

        verify(userService, times(0)).switchUserRoleToUser(any());
        verify(userService, times(0)).switchUserRoleToAdmin(any());
    }
}
