package app.newellgames.web;

import app.newellgames.config.CustomAuthenticationFailureHandler;
import app.newellgames.security.AuthenticationMetadata;
import app.newellgames.transaction.model.Transaction;
import app.newellgames.transaction.service.TransactionService;
import app.newellgames.user.model.User;
import app.newellgames.user.model.UserRole;
import app.newellgames.user.service.UserService;
import app.newellgames.web.controllers.ShopController;
import app.newellgames.web.controllers.TransactionController;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@Import(CustomAuthenticationFailureHandler.class)
public class TransactionControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TransactionService transactionService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRequestToTransactionsMyTransactionsEndpoint_ShouldReturnTransactionsView() throws Exception {
        UUID userId = UUID.randomUUID();

        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/transactions/my-transactions")
                .with(user(principal));

        when(userService.getById(any())).thenReturn(aRandomUser());
        when(userService.getTransactions(any())).thenReturn(List.of(aRandomTransaction()));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("transactions"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("myTransactions"));
    }

    @Test
    void getRequestToAllTransactionsEndpoint_WithAuthorizedAdmin_ShouldReturnAllTransactionsView() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = aRandomUser();
        Transaction myTransaction = aRandomTransaction();

        myTransaction.setOwner(user);

        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.ADMIN, true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/transactions/admin/all-transactions")
                .with(user(principal));

        when(transactionService.getAllTransactions()).thenReturn(List.of(myTransaction));
        when(userService.getById(any())).thenReturn(user);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("all-transactions"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("allTransactions"));

        verify(transactionService, times(0)).getTransactionByOwnerIdOrUsername(any());
    }

    @Test
    void getRequestToAllTransactionsEndpoint_WithAuthorizedAdminAndParam_ShouldReturnAllTransactionsView() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = aRandomUser();
        Transaction myTransaction = aRandomTransaction();

        myTransaction.setOwner(user);

        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.ADMIN, true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/transactions/admin/all-transactions").param("query", "Not Blank")
                .with(user(principal));

        when(transactionService.getTransactionByOwnerIdOrUsername(any())).thenReturn(List.of(myTransaction));
        when(userService.getById(any())).thenReturn(user);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("all-transactions"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("allTransactions"))
                .andExpect(model().attributeExists("query"));

        verify(transactionService, times(0)).getAllTransactions();
    }

    @Test
    void getRequestToAllTransactionsEndpoint_WithoutAuthorizedAdmin_ShouldReturnNotFoundView() throws Exception {
        UUID userId = UUID.randomUUID();

        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/transactions/admin/all-transactions")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().is4xxClientError())
                .andExpect(view().name("not-found"));
    }
}
