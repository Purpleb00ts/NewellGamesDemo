package app.newellgames.web.controllers;

import app.newellgames.security.AuthenticationMetadata;
import app.newellgames.user.model.User;
import app.newellgames.user.service.UserService;
import app.newellgames.utility.DtoMapper;
import app.newellgames.web.dto.EditProfileRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    public final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ModelAndView getProfile(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        User user = userService.getById(id);

        if (!authenticationMetadata.getUserId().equals(id)) {
            throw new AccessDeniedException("You are not allowed to view other users' reviews");
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("profile");
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @GetMapping("/{id}/edit-profile")
    public ModelAndView getEditProfile(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        User user = userService.getById(id);

        if (!authenticationMetadata.getUserId().equals(id)) {
            throw new AccessDeniedException("You are not allowed to view other users' reviews");
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("edit-profile");
        modelAndView.addObject("user", user);
        modelAndView.addObject("editProfileRequest", DtoMapper.mapUserToUserEditRequest(user));

        return modelAndView;
    }

    @PutMapping ("/{id}/edit-profile")
    public ModelAndView editProfile(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata, @Valid EditProfileRequest editProfileRequest, BindingResult bindingResult) {
        User user = userService.getById(authenticationMetadata.getUserId());

        if (!authenticationMetadata.getUserId().equals(id)) {
            throw new AccessDeniedException("Nice try, but you are not allowed to do that!");
        }

        if(bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("edit-profile");
            modelAndView.addObject("user", user);
            modelAndView.addObject("editProfileRequest", editProfileRequest);
            modelAndView.addObject("errors", bindingResult);
            return modelAndView;
        }

        userService.editProfile(user, editProfileRequest);

        return new ModelAndView ("redirect:/profile/" + user.getId());
    }
}
