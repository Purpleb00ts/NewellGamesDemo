package app.newellgames.web.controllers;

import app.newellgames.security.AuthenticationMetadata;
import app.newellgames.user.model.User;
import app.newellgames.user.service.UserService;
import app.newellgames.utility.DtoMapper;
import app.newellgames.web.dto.LoginRequest;
import app.newellgames.web.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {
    private final UserService userService;

    @Autowired
    public IndexController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/")
    public String getIndexPage() {

        return "index";
    }

    @GetMapping("/login")
    public ModelAndView getLoginPage(@RequestParam(value = "error", required = false) String error, HttpServletRequest request) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        modelAndView.addObject("loginRequest", DtoMapper.mapLoginToLoginRequest());

        String loginError = (String) request.getSession().getAttribute("LOGIN_ERROR");
        if (loginError != null) {
            modelAndView.addObject("errorMessage", loginError);
            request.getSession().removeAttribute("LOGIN_ERROR"); // prevent re-showing
        }

//        if (error != null) {
//            modelAndView.addObject("errorMessage", "Incorrect username or password!");
//        }

        return modelAndView;
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register");
        modelAndView.addObject("registerRequest", DtoMapper.mapRegisterToRegisterRequest());

        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView registerNewUser(@Valid RegisterRequest registerRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ModelAndView("register");
        }

        userService.register(registerRequest);

        return new ModelAndView("redirect:/login");
    }


}
