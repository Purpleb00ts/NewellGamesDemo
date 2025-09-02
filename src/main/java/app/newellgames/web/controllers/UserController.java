package app.newellgames.web.controllers;

import app.newellgames.security.AuthenticationMetadata;
import app.newellgames.user.model.User;
import app.newellgames.user.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping ("/admin/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView getAllUsers(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata, @RequestParam(value = "query", required = false) String query) {

        User user = userService.getById(authenticationMetadata.getUserId());

        List<User> users;

        if(query != null && !query.isBlank()) {
            users = userService.getUserByUsernameOrIdOrEmail(query);
        } else {
            users = userService.getAllUsers();
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("users");
        modelAndView.addObject("user", user);
        modelAndView.addObject("users", users);
        modelAndView.addObject("query", query);

        return modelAndView;
    }

    @PostMapping("/switch-role/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String switchRole(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                             @RequestParam(required = false) String isAdmin, @PathVariable UUID id) {
        User user = userService.getById(id);

        if ("on".equals(isAdmin)) {
            userService.switchUserRoleToAdmin(user);
        } else {
            userService.switchUserRoleToUser(user);
        }

        return "redirect:/admin/users";
    }

    @PostMapping ("/switch-status/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String switchStatus(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata, @RequestParam(required = false) String isActive, @PathVariable UUID id)
    {
        User user = userService.getById(id);

        if("on".equals(isActive)) {
            userService.switchUserStatusToActive(user);
        } else {
            userService.switchUserStatusToInActive(user);
        }

        return "redirect:/admin/users";
    }
}
