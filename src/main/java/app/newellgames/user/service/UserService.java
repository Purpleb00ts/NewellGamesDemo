package app.newellgames.user.service;

import app.newellgames.cart.model.Cart;
import app.newellgames.cart.model.CartItem;
import app.newellgames.cart.service.CartService;
import app.newellgames.exception.*;
import app.newellgames.game.model.Game;
import app.newellgames.notification.service.NotificationService;
import app.newellgames.review.model.Review;
import app.newellgames.security.AuthenticationMetadata;
import app.newellgames.transaction.model.Transaction;
import app.newellgames.transaction.model.TransactionStatus;
import app.newellgames.transaction.model.TransactionType;
import app.newellgames.transaction.service.TransactionService;
import app.newellgames.user.model.User;
import app.newellgames.user.model.UserRole;
import app.newellgames.user.repository.UserRepository;
import app.newellgames.utility.UuidUtility;
import app.newellgames.web.dto.DepositRequest;
import app.newellgames.web.dto.EditProfileRequest;
import app.newellgames.web.dto.RegisterRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final CartService cartService;
    private final PasswordEncoder passwordEncoder;
    private final TransactionService transactionService;
    private final NotificationService notificationService;

    @Autowired
    public UserService(UserRepository userRepository, CartService cartService, PasswordEncoder passwordEncoder, TransactionService transactionService, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.cartService = cartService;
        this.passwordEncoder = passwordEncoder;
        this.transactionService = transactionService;
        this.notificationService = notificationService;
    }

    // Register the user, assign him a cart, save the default notification preference (save user to DB of a monolith and save his notification preference into MicroService's DB)
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public User register(RegisterRequest registerRequest) {
        Optional<User> optionalUser = userRepository.findByUsername(registerRequest.getUsername());
        if (optionalUser.isPresent()) {
            throw new UsernameAlreadyExistException("Username %s already exist.".formatted(registerRequest.getUsername()), "/register");
        }

        User user = initializeUser(registerRequest);

        userRepository.save(user);

        user.setCart(cartService.createCartForUser(user));

        notificationService.saveNotificationPreference(user.getId(), false, null);

        return user;
    }

    // Method for initializing default user (with client's input) for registration through DTO
    public User initializeUser(RegisterRequest registerRequest) {

        return User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.USER)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .balance(BigDecimal.ZERO)
                .build();
    }

    // Edit profile functionality
    // - Edit Username
    // - Edit Email
    // - Edit Profile Picture
    // I allow users to register without an email, but if they want to have notifications, they have to add their email
    @CacheEvict(value = "users", allEntries = true)
    public void editProfile(User user, EditProfileRequest editProfileRequest) {
        User userToEdit = getById(user.getId());

        List<String> errors = new ArrayList<>();

        boolean usernameChanged = !userToEdit.getUsername().equals(editProfileRequest.getUsername());
        boolean emailChanged = true;

        if (userToEdit.getEmail() != null && editProfileRequest.getEmail() != null) {
            emailChanged = !userToEdit.getEmail().equals(editProfileRequest.getEmail());
        }

        if (usernameChanged) {
            Optional<User> userWithSameUsername = userRepository.findByUsername(editProfileRequest.getUsername());
            if (userWithSameUsername.isPresent() && !userWithSameUsername.get().getId().equals(userToEdit.getId())) {
                errors.add("Username '%s' is already taken.".formatted(editProfileRequest.getUsername()));
            } else {
                userToEdit.setUsername(editProfileRequest.getUsername());
            }
        }

        if (emailChanged) {
            Optional<User> userWithSameEmail = userRepository.findByEmail(editProfileRequest.getEmail());
            if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(userToEdit.getId())) {
                errors.add("Email '%s' is already in use.".formatted(editProfileRequest.getEmail()));
            } else {
                if(!editProfileRequest.getEmail().isBlank()){
                    userToEdit.setEmail(editProfileRequest.getEmail());
                } else {
                    userToEdit.setEmail(null);
                }
            }
        }

        userToEdit.setProfilePicture(editProfileRequest.getProfilePicture());

        userRepository.save(userToEdit);

        if (checkIfUserHasEmail(userToEdit) && (emailChanged || usernameChanged)) {
            notificationService.saveNotificationPreference(
                    userToEdit.getId(),
                    notificationService.getNotificationPreference(userToEdit.getId()).isEnabled(),
                    userToEdit.getEmail()
            );

            notificationService.sendNotification(
                    userToEdit.getId(),
                    "Your account's information has been updated.",
                    "You've successfully updated your account information.\nEmail: %s\nUsername: %s\n"
                            .formatted(userToEdit.getEmail(), userToEdit.getUsername())
            );
        }

        if (!errors.isEmpty()) {
            if (errors.contains("Username '%s' is already taken.".formatted(editProfileRequest.getUsername()))) {
                throw new UsernameAlreadyExistException("Username %s is already taken.".formatted(editProfileRequest.getUsername()), "/profile/" + user.getId() + "/edit-profile");
            }
            if (errors.contains("Email '%s' is already in use.".formatted(editProfileRequest.getEmail()))) {
                throw new EmailAlreadyExistException("Email '%s' is already in use.".formatted(editProfileRequest.getEmail()), user.getId());
            }
        }
    }

    // Method responsible for topping up the user's balance
    // Saves a transaction in the DB, user's can see a list with their transactions and Admin can see all transactions that occurred in the app
    // Sends notification to the user if they have assigned email and enabled notifications
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void topUp (User user, DepositRequest depositRequest) {
        if (user.isActive()) {
            user.setBalance(user.getBalance().add(depositRequest.getAmount()));
            userRepository.save(user);
            transactionService.initializeTransaction(user, depositRequest.getAmount(), TransactionStatus.SUCCESSFUL, TransactionType.DEPOSIT);
            notificationService.sendNotification(user.getId(), "Your deposit has been successful!", "Thank you, %s! Funds were added to your account's balance.".formatted(user.getUsername()));
            return;
        }

        transactionService.initializeTransaction(user, depositRequest.getAmount(), TransactionStatus.FAILED, TransactionType.DEPOSIT);
        notificationService.sendNotification(user.getId(), "Something went wrong!", "We apologize, %s! Funds weren't added to your account's balance. Please, try again later.".formatted(user.getUsername()));
        throw new FailedTopUpException("There was an error with top-up, funds weren't added.");
    }

    // Method for purchasing all items in user's cart
    // Saves a transaction in the DB
    // Adds games to the user's library
    // Clears his cart
    // Sends a notification with a list of purchased games
    @Transactional
    public void purchaseCartItems (User user) {
        Cart cart = user.getCart();
        BigDecimal totalPrice = cartService.calculateTotalPrice(cart);
        if (totalPrice.compareTo(user.getBalance()) > 0) {
            transactionService.initializeTransaction(user, totalPrice, TransactionStatus.FAILED, TransactionType.PURCHASE);
            return;
        }

        try {
            List<String> gameTitles = cart.getItems().stream().map(CartItem::getGame).map(Game::getTitle).toList();

            StringBuilder sb = new StringBuilder();

            for (String title : gameTitles) {
                sb.append("- %s%n".formatted(title));
            }

            notificationService.sendNotification(user.getId(), "Thank you for the purchase!", "Thank you! Games were added to your library!%n%s".formatted(sb.toString()));

            addPurchasedGames(cart, user);

            transactionService.initializeTransaction(user, totalPrice, TransactionStatus.SUCCESSFUL, TransactionType.PURCHASE);

            cartService.clearAllItemsFromCart(user);
        } catch (Exception e) {
            throw new FailedPurchaseException("Something went wrong with purchasing these games.");
        }
    }

    // Method that makes the purchase and deducts the total price of cart from user's balance
    @CacheEvict(value = "users", allEntries = true)
    public void addPurchasedGames(Cart cart, User user) {
        List<CartItem> cartItems = cart.getItems();
        for (CartItem cartItem : cartItems) {
            user.getMyGames().add(cartItem.getGame());
        }

        user.setBalance(user.getBalance().subtract(cartService.calculateTotalPrice(cart)));

        userRepository.save(user);
    }

    // Method that assigns the review to the user (used by ReviewService addReview method)
    // Sends a notification to the user that review has been added and it's content
    @CacheEvict(value = "users", allEntries = true)
    public void addUsersReview (Review review, User user) {
        try {
            user.getMyReviews().add(review);
            userRepository.save(user);
            notificationService.sendNotification(user.getId(), "Your review has been submitted.", "You've successfully added a review to the game %s.%nTitle: %s%nReview: %s%n".formatted(review.getGame().getTitle(), review.getTitle(), review.getReviewDescription()));
        } catch (Exception e) {
            throw new FailedToPostReviewException("Unfortunately something went wrong and your review wasn't posted.", user.getId());
        }
    }

    // Simple check if user owns the game and is allowed to leave a review on it
    public boolean checkIfUserOwnsTheGame (User user, Game game) {
        return user.getMyGames().contains(game);
    }

    // Switches user's role to Admin
    @CacheEvict(value = "users", allEntries = true)
    public void switchUserRoleToAdmin (User user) {
        user.setRole(UserRole.ADMIN);
        userRepository.save(user);
    }

    // Switches user's role to User
    @CacheEvict(value = "users", allEntries = true)
    public void switchUserRoleToUser (User user) {
        user.setRole(UserRole.USER);
        userRepository.save(user);
    }

    // Switches user's status to Active
    @CacheEvict(value = "users", allEntries = true)
    public void switchUserStatusToActive(User user) {
        user.setActive(true);
        userRepository.save(user);
    }

    // Switches user's status to Inactive
    @CacheEvict(value = "users", allEntries = true)
    public void switchUserStatusToInActive(User user) {
        user.setActive(false);
        userRepository.save(user);
    }

    // Checks if user has email and used by EditProfile method
    public boolean checkIfUserHasEmail (User user) {
        return user.getEmail() != null;
    }

    // Gets user from DB by his ID
    public User getById(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new DomainException("User with id [%s] does not exist.".formatted(userId)));
    }

    // Gets all users from DB and orders them by username ascending
    @Cacheable("users")
    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByUsernameAsc();
    }

    // Gets users by ID, username or Email so Admin can search for a user in Users tab (shows all registered users to Admin)
    public List<User> getUserByUsernameOrIdOrEmail(String query) {
        if (UuidUtility.isValid(query)) {
            return userRepository.findById(UUID.fromString(query)).stream().toList();
        } else {
            return userRepository.searchByUsernameOrEmail(query);
        }
    }

    // Returns a set with game IDs that user own, used in Shop Controller so if user owns a game he can't buy it again
    public Set<UUID> getOwnedGameIds(User user) {
        Set<UUID> ownedGameIds = new HashSet<>();
        for (Game game : user.getMyGames()) {
            ownedGameIds.add(game.getId());
        }
        return ownedGameIds;
    }

    // Gets all transactions that user made, every user can see all of their transactions
    public List<Transaction> getTransactions(User user) {
        return transactionService.getAllTransactionsByUser(user);
    }

    // Loads our principle
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User with this username does not exist."));

        return new AuthenticationMetadata(user.getId(), username, user.getPassword(), user.getRole(), user.isActive());
    }
}
