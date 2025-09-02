package app.newellgames.web;

import app.newellgames.cart.service.CartService;
import app.newellgames.config.CustomAuthenticationFailureHandler;
import app.newellgames.game.model.Game;
import app.newellgames.game.service.GameService;
import app.newellgames.security.AuthenticationMetadata;
import app.newellgames.user.model.UserRole;
import app.newellgames.user.service.UserService;
import app.newellgames.web.controllers.ReviewController;
import app.newellgames.web.controllers.ShopController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static app.newellgames.TestBuilder.aRandomGame;
import static app.newellgames.TestBuilder.aRandomUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShopController.class)
@Import(CustomAuthenticationFailureHandler.class)
public class ShopControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private GameService gameService;

    @MockitoBean
    private CartService cartService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRequestToShopEndpoint_ShouldReturnShopView () throws Exception {
        UUID userId = UUID.randomUUID();
        Game aRandomGame = aRandomGame();
        aRandomGame.setId(UUID.randomUUID());

        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/shop")
                .with(user(principal));

        when(userService.getById(any())).thenReturn(aRandomUser());
        when(userService.getOwnedGameIds(any())).thenReturn(Set.of(aRandomGame.getId()));
        when(cartService.getGamesForUser(any())).thenReturn(Set.of(aRandomGame));
        when(gameService.getAllGames()).thenReturn(List.of(aRandomGame));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("shop"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("allGames"))
                .andExpect(model().attributeExists("ownedGameIds"))
                .andExpect(model().attributeExists("gamesAlreadyInCart"));

        verify(gameService, times(0)).findGamesByTitle(any());
    }

    @Test
    void getRequestToShopEndpoint_ShouldReturnShopView_WithSearchedGame () throws Exception {
        UUID userId = UUID.randomUUID();
        Game aRandomGame = aRandomGame();
        aRandomGame.setId(UUID.randomUUID());

        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/shop").param("query", "Elden Ring")
                .with(user(principal));

        when(userService.getById(any())).thenReturn(aRandomUser());
        when(userService.getOwnedGameIds(any())).thenReturn(Set.of(aRandomGame.getId()));
        when(cartService.getGamesForUser(any())).thenReturn(Set.of(aRandomGame));
        when(gameService.findGamesByTitle(any())).thenReturn(List.of(aRandomGame));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("shop"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("allGames"))
                .andExpect(model().attributeExists("query"))
                .andExpect(model().attributeExists("ownedGameIds"))
                .andExpect(model().attributeExists("gamesAlreadyInCart"));

        verify(gameService, times(0)).getAllGames();
    }

    @Test
    void postRequestToShopAddToCartEndpoint_ShouldReturnShopView () throws Exception {
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/shop/add-to-cart/{id}", gameId).param("query", "Elden Ring")
                .with(user(principal))
                .with(csrf());

        when(gameService.getById(any())).thenReturn(aRandomGame());
        when(userService.getById(any())).thenReturn(aRandomUser());
        doNothing().when(cartService).addGameToCart(any(), any());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/shop"));

        verify(cartService, times(1)).addGameToCart(any(), any());
    }
}
