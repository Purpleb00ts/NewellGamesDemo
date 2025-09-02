package app.newellgames.cart.service;

import app.newellgames.cart.model.Cart;
import app.newellgames.cart.model.CartItem;
import app.newellgames.cart.repository.CartItemRepository;
import app.newellgames.cart.repository.CartRepository;
import app.newellgames.game.model.Game;
import app.newellgames.game.repository.GameRepository;
import app.newellgames.game.service.GameService;
import app.newellgames.transaction.model.TransactionStatus;
import app.newellgames.transaction.model.TransactionType;
import app.newellgames.transaction.service.TransactionService;
import app.newellgames.user.model.User;
import app.newellgames.user.repository.UserRepository;
import app.newellgames.user.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    // Creates and assigns a cart to the user, saves it into the DB
    @Transactional
    public Cart createCartForUser (User user) {
        Cart cart = Cart.builder()
                .user(user)
                .build();
        user.setCart(cart);
        cartRepository.save(cart);

        return cart;
    }

    // Creates cart item, assigns it to the user's cart and saves it into DB
    public void addGameToCart(Game game, User user) {

        CartItem gameToAdd = CartItem.builder()
                .game(game)
                .price(game.getPrice())
                .cart(user.getCart())
                .build();


        cartItemRepository.save(gameToAdd);
   }

   // Removes a cart item from user's cart
   public void removeGameFromCart(CartItem cartItem, User user) {
        user.getCart().getItems().remove(cartItem);

        cartItemRepository.delete(cartItem);
   }

   // Clears all cart items from user's cart
   @Transactional
   public void clearAllItemsFromCart (User user) {
        Cart cart = user.getCart();
        cart.getItems().clear();

        cartRepository.save(cart);
   }

   // Calculates total price of the cart
    public BigDecimal calculateTotalPrice(Cart cart) {
        return cart.getItems().stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Returns a set with games that are already in user's cart
    public Set<Game> getGamesForUser (Cart cart) {
        Set<Game> gamesAlreadyInCart = new HashSet<>();
        for (CartItem item : cart.getItems()) {
            gamesAlreadyInCart.add(item.getGame());
        }

        return gamesAlreadyInCart;
    }

    // Gets cart item by ID
   public CartItem getCartItem(UUID id) {
        return cartItemRepository.findById(id).orElse(null);
   }
    // Gets cart by ID
   public Cart getCart(UUID id) {
        return cartRepository.findById(id).orElse(null);
   }
}
