package app.newellgames.web.controllers;

import app.newellgames.game.model.Game;
import app.newellgames.game.service.GameService;
import app.newellgames.review.service.ReviewService;
import app.newellgames.security.AuthenticationMetadata;
import app.newellgames.user.model.User;
import app.newellgames.user.service.UserService;
import app.newellgames.utility.DtoMapper;
import app.newellgames.web.dto.AddReviewRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final GameService gameService;
    private final UserService userService;

    public ReviewController(ReviewService reviewService, GameService gameService, UserService userService) {
        this.reviewService = reviewService;
        this.gameService = gameService;
        this.userService = userService;
    }

    @GetMapping ("/{id}")
    public ModelAndView showReviewsForTheGame (@PathVariable UUID id, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        Game game = gameService.getById(id);
        User user = userService.getById(authenticationMetadata.getUserId());
        boolean userOwnsThisGame = userService.checkIfUserOwnsTheGame(user, game);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("reviews");
        modelAndView.addObject("user", user);
        modelAndView.addObject("game", game);
        modelAndView.addObject("reviews", game.getReviews());
        modelAndView.addObject("userOwnsThisGame", userOwnsThisGame);

        return modelAndView;
    }

    @GetMapping ("/{id}/add-review")
    public ModelAndView getAddReviewForm(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        User user = userService.getById(authenticationMetadata.getUserId());
        Game game = gameService.getById(id);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("add-review");
        modelAndView.addObject("user", user);
        modelAndView.addObject("game", game);
        modelAndView.addObject("addReviewRequest", DtoMapper.mapReviewToAddReviewRequest());

        return modelAndView;
    }

    @GetMapping ("/my-reviews")
    public ModelAndView getMyReviewsPage (@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        User user = userService.getById(authenticationMetadata.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("my-reviews");
        modelAndView.addObject("user", user);
        modelAndView.addObject("reviews", user.getMyReviews());

        return modelAndView;
    }

    @PostMapping("{id}/add-review")
    public String addReview (@PathVariable UUID id, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata, @Valid AddReviewRequest addReviewRequest, BindingResult bindingResult) {
        User user = userService.getById(authenticationMetadata.getUserId());
        Game game = gameService.getById(id);


        if (bindingResult.hasErrors()) {
            return "redirect:/reviews/" + id;
        }

        reviewService.addReview(user, game, addReviewRequest);

        return "redirect:/reviews/" + id;
    }

}
