package app.newellgames.web;


import app.newellgames.config.CustomAuthenticationFailureHandler;
import app.newellgames.game.model.Game;
import app.newellgames.game.service.GameService;
import app.newellgames.review.model.Review;
import app.newellgames.review.model.ReviewType;
import app.newellgames.review.service.ReviewService;
import app.newellgames.security.AuthenticationMetadata;
import app.newellgames.user.model.User;
import app.newellgames.user.model.UserRole;
import app.newellgames.user.service.UserService;
import app.newellgames.web.controllers.ReviewController;
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

import static app.newellgames.TestBuilder.aRandomUser;
import static app.newellgames.TestBuilder.aRandomGame;
import static app.newellgames.TestBuilder.aRandomReview;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@Import(CustomAuthenticationFailureHandler.class)
public class ReviewControllerApiTest {

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private GameService gameService;

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRequestToReviewsEndpoint_ShouldReturnReviewsForASingleGame () throws Exception {
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        User aRandomUser = aRandomUser();
        Game aRandomGame = aRandomGame();
        Review aRandomReview = aRandomReview();

        aRandomReview.setGame(aRandomGame);
        aRandomReview.setAuthor(aRandomUser);

        aRandomGame.setReviews(List.of(aRandomReview));

        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/reviews/{id}", gameId)
                .with(user(principal));

        when(gameService.getById(any())).thenReturn(aRandomGame);
        when(userService.getById(any())).thenReturn(aRandomUser);
        when(userService.checkIfUserOwnsTheGame(any(), any())).thenReturn(true);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("reviews"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("game"))
                .andExpect(model().attributeExists("reviews"))
                .andExpect(model().attributeExists("userOwnsThisGame"));
    }

    @Test
    void getRequestToReviewsAddReviewEndpoint_ShouldReturnAddReviewsView () throws Exception {

        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/reviews/{id}/add-review", gameId)
                .with(user(principal));

        when(gameService.getById(any())).thenReturn(aRandomGame());
        when(userService.getById(any())).thenReturn(aRandomUser());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("add-review"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("game"))
                .andExpect(model().attributeExists("addReviewRequest"));
    }

    @Test
    void getRequestToMyReviewsEndpoint_ShouldReturnMyReviewsView () throws Exception {
        UUID userId = UUID.randomUUID();

        Game aRandomGame = aRandomGame();
        User aRandomUser = aRandomUser();
        Review aRandomReview = aRandomReview();

        aRandomReview.setGame(aRandomGame);
        aRandomReview.setAuthor(aRandomUser);
        aRandomUser.setMyReviews(List.of(aRandomReview));

        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/reviews/my-reviews")
                .with(user(principal));

        when(userService.getById(any())).thenReturn(aRandomUser);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("my-reviews"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("reviews"));
    }

    @Test
    void postRequestToReviewsAddReviewEndpoint_HappyPath_ShouldRedirectToReviewsView () throws Exception {
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/reviews/{id}/add-review", gameId)
                .formField("title", "randomTitle")
                .formField("description", "randomDescription")
                .formField("type", "POSITIVE")
                .with(user(principal))
                .with(csrf());

        when(gameService.getById(any())).thenReturn(aRandomGame());
        when(userService.getById(any())).thenReturn(aRandomUser());
        doNothing().when(reviewService).addReview(any(), any(), any());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reviews/" + gameId));

        verify(reviewService, times(1)).addReview(any(), any(), any());
    }

    @Test
    void postRequestToReviewsAddReviewEndpoint_InvalidData_ShouldRedirectToReviewsView () throws Exception {
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/reviews/{id}/add-review", gameId)
                .formField("title", "")
                .formField("description", "")
                .formField("type", "")
                .with(user(principal))
                .with(csrf());

        when(gameService.getById(any())).thenReturn(aRandomGame());
        when(userService.getById(any())).thenReturn(aRandomUser());
        doNothing().when(reviewService).addReview(any(), any(), any());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reviews/" + gameId));

        verify(reviewService, times(0)).addReview(any(), any(), any());
    }
}
