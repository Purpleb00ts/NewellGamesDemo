package app.newellgames.web;

import app.newellgames.cart.model.CartItem;
import app.newellgames.cart.service.CartService;
import app.newellgames.config.CustomAuthenticationFailureHandler;
import app.newellgames.security.AuthenticationMetadata;
import app.newellgames.user.model.User;
import app.newellgames.user.model.UserRole;
import app.newellgames.user.service.UserService;
import app.newellgames.web.controllers.CartController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

import java.math.BigDecimal;
import java.util.UUID;

import static app.newellgames.TestBuilder.aRandomUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@Import(CustomAuthenticationFailureHandler.class)
public class CartControllerApiTest {

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRequestToCartEndpoint_ShouldReturnCartView () throws Exception {

        when(userService.getById(any())).thenReturn(aRandomUser());
        when(cartService.calculateTotalPrice(any())).thenReturn(BigDecimal.ONE);

        UUID userId = UUID.randomUUID();
        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/cart")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("totalPrice"))
                .andExpect(model().attributeExists("cartItems"));
    }

    @Test
    void postRequestToCartRemoveEndpoint_ShouldRemoveCartItem() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID cartItemId = UUID.randomUUID();

        AuthenticationMetadata principal =
                new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);

        User mockUser = aRandomUser();
        when(userService.getById(userId)).thenReturn(mockUser);

        CartItem mockCartItem = new CartItem();
        when(cartService.getCartItem(cartItemId)).thenReturn(mockCartItem);


        doNothing().when(cartService).removeGameFromCart(mockCartItem, mockUser);


        mockMvc.perform(post("/cart/remove/{cartItemId}", cartItemId)
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));


        verify(userService).getById(userId);
        verify(cartService).getCartItem(cartItemId);
        verify(cartService).removeGameFromCart(mockCartItem, mockUser);
    }

    @Test
    void postRequestToCartClearEndpoint_ShouldRemoveAllCartItems() throws Exception {
        UUID userId = UUID.randomUUID();

        AuthenticationMetadata principal =
                new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);

        User mockUser = aRandomUser();
        when(userService.getById(userId)).thenReturn(mockUser);

        doNothing().when(cartService).clearAllItemsFromCart(mockUser);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/cart/clear")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));


        verify(userService).getById(userId);
        verify(cartService).clearAllItemsFromCart(mockUser);
    }

    @Test
    void postRequestToCartPurchaseEndpoint_ShouldPurchaseAllCartItems() throws Exception {
        UUID userId = UUID.randomUUID();

        AuthenticationMetadata principal =
                new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);

        User mockUser = aRandomUser();
        when(userService.getById(userId)).thenReturn(mockUser);

        doNothing().when(userService).purchaseCartItems(mockUser);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/cart/purchase")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));


        verify(userService).getById(userId);
        verify(userService).purchaseCartItems(mockUser);
    }
}
