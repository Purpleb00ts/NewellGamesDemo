package app.newellgames.web.controllers;

import app.newellgames.cart.model.Cart;
import app.newellgames.cart.model.CartItem;
import app.newellgames.cart.service.CartService;
import app.newellgames.game.model.Game;
import app.newellgames.security.AuthenticationMetadata;
import app.newellgames.user.model.User;
import app.newellgames.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/cart")
public class CartController {
    private final UserService userService;
    private final CartService cartService;

    @Autowired
    public CartController(UserService userService, CartService cartService) {
        this.userService = userService;
        this.cartService = cartService;
    }

    @GetMapping
    public ModelAndView getCartPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        User user = userService.getById(authenticationMetadata.getUserId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("cart");
        modelAndView.addObject("user", user);
        modelAndView.addObject("totalPrice", cartService.calculateTotalPrice(user.getCart()));
        modelAndView.addObject("cartItems", user.getCart().getItems());


        return modelAndView;
    }

    @PostMapping("/remove/{cartItemId}")
    public String removeItemFromCart (@PathVariable UUID cartItemId, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        User user = userService.getById(authenticationMetadata.getUserId());

        CartItem cartItem = cartService.getCartItem(cartItemId);

        cartService.removeGameFromCart(cartItem, user);

        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        User user = userService.getById(authenticationMetadata.getUserId());

        cartService.clearAllItemsFromCart(user);

        return "redirect:/cart";
    }

    @PostMapping ("/purchase")
    public String purchase(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        User user = userService.getById(authenticationMetadata.getUserId());

        Cart cart = user.getCart();

        userService.purchaseCartItems(user);

        return "redirect:/cart";
    }
}
