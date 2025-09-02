package app.newellgames.web.controllers;

import app.newellgames.security.AuthenticationMetadata;
import app.newellgames.transaction.service.TransactionService;
import app.newellgames.user.model.User;
import app.newellgames.user.service.UserService;
import app.newellgames.utility.DtoMapper;
import app.newellgames.web.dto.DepositRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/add-funds")
public class DepositController {
    private final UserService userService;

    public DepositController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView deposit(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        User user = userService.getById(authenticationMetadata.getUserId());
        ModelAndView modelAndView = new ModelAndView("add-funds");

        modelAndView.addObject("user", user);
        modelAndView.addObject("depositRequest", DtoMapper.mapDepositToDepositRequest());

        return modelAndView;
    }

    @PostMapping
    public ModelAndView topUp(@Valid DepositRequest depositRequest, BindingResult bindingResult, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        User user = userService.getById(authenticationMetadata.getUserId());

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("add-funds");
            modelAndView.addObject("user", user);
            return modelAndView;
        }


        userService.topUp(user, depositRequest);
        ModelAndView modelAndView = new ModelAndView("add-funds");
        modelAndView.addObject("user", user);
        modelAndView.addObject("depositRequest", DtoMapper.mapDepositToDepositRequest());
        modelAndView.addObject("success", true);
        return modelAndView;
    }
}
