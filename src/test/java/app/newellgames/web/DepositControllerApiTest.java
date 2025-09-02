package app.newellgames.web;

import app.newellgames.config.CustomAuthenticationFailureHandler;
import app.newellgames.security.AuthenticationMetadata;
import app.newellgames.user.model.UserRole;
import app.newellgames.user.service.UserService;
import app.newellgames.web.controllers.CartController;
import app.newellgames.web.controllers.DepositController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static app.newellgames.TestBuilder.aRandomUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepositController.class)
@Import(CustomAuthenticationFailureHandler.class)
public class DepositControllerApiTest {

    @MockitoBean
    UserService userService;

    @Autowired
    MockMvc mockMvc;

    @Test
    void getRequestToAddFundsEndpoint_ShouldReturnAddFundsView() throws Exception {

        when(userService.getById(any())).thenReturn(aRandomUser());

        UUID userId = UUID.randomUUID();
        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/add-funds")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("depositRequest"))
                .andExpect(view().name("add-funds"));
    }

    @Test
    void postRequestToAddFundsEndpoint_happyPath() throws Exception {

        UUID userId = UUID.randomUUID();
        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/add-funds")
                .formField("amount", "100")
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());
        doNothing().when(userService).topUp(any(), any());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("depositRequest"))
                .andExpect(model().attributeExists("success"))
                .andExpect(view().name("add-funds"));

        verify(userService, times(1)).topUp(any(), any());
    }

    @Test
    void postRequestToAddFundsEndpoint_whenGivenIncorrectAmount() throws Exception {

        UUID userId = UUID.randomUUID();
        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/add-funds")
                .formField("amount", "10000")
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user"))
                .andExpect(view().name("add-funds"));

        verify(userService, times(0)).topUp(any(), any());
    }
}
