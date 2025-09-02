package app.newellgames.web.controllers;

import app.newellgames.game.model.Game;
import app.newellgames.game.service.GameService;
import app.newellgames.security.AuthenticationMetadata;
import app.newellgames.user.model.User;
import app.newellgames.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/library")
public class LibraryController {
    public final GameService gameService;
    public final UserService userService;

    public LibraryController(GameService gameService, UserService userService) {
        this.gameService = gameService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView getLibraryPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        User user = userService.getById(authenticationMetadata.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("library");
        modelAndView.addObject("user", user);
        modelAndView.addObject("myGames", user.getMyGames());

        return modelAndView;
    }
}
