package app.newellgames.web.controllers;

import app.newellgames.cart.service.CartService;
import app.newellgames.game.model.Game;
import app.newellgames.game.service.GameService;
import app.newellgames.security.AuthenticationMetadata;
import app.newellgames.user.model.User;
import app.newellgames.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Controller
@RequestMapping("/shop")
public class ShopController {

    private final UserService userService;
    private final GameService gameService;
    private final CartService cartService;

    @Autowired
    public ShopController(UserService userService, GameService gameService, CartService cartService) {
        this.userService = userService;
        this.gameService = gameService;
        this.cartService = cartService;
    }

//    @GetMapping
//    public ModelAndView shop(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
//        User user = userService.getById(authenticationMetadata.getUserId());
//
//        List<Game> allGames = gameService.getAllGames();
//
//        ModelAndView modelAndView = new ModelAndView();
//        modelAndView.setViewName("shop");
//        modelAndView.addObject("user", user);
//        modelAndView.addObject("allGames", allGames);
//
//        return modelAndView;
//    }

    @GetMapping
    public ModelAndView searchGames(@RequestParam(value = "query", required = false) String query, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        User user = userService.getById(authenticationMetadata.getUserId());
        List<Game> allGames;
        Set<UUID> ownedGameIds = userService.getOwnedGameIds(user);
        Set<Game> gamesAlreadyInCart = cartService.getGamesForUser(user.getCart());

        if (query != null && !query.isBlank()) {
            allGames = gameService.findGamesByTitle(query);
        } else {
            allGames = gameService.getAllGames();
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("shop");
        modelAndView.addObject("user", user);
        modelAndView.addObject("allGames", allGames);
        modelAndView.addObject("query", query);
        modelAndView.addObject("ownedGameIds", ownedGameIds);
        modelAndView.addObject("gamesAlreadyInCart", gamesAlreadyInCart);

        return modelAndView; // the Thymeleaf view
    }



    @PostMapping("/add-to-cart/{id}")
    public String addGameToCart (@PathVariable UUID id, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        User user = userService.getById(authenticationMetadata.getUserId());

        Game game = gameService.getById(id);

        cartService.addGameToCart(game, user);

        return "redirect:/shop";
    }
}
