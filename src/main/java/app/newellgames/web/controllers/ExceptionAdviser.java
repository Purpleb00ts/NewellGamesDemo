package app.newellgames.web.controllers;

import app.newellgames.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class ExceptionAdviser {

    @ExceptionHandler(UsernameAlreadyExistException.class)
    public String usernameAlreadyExistException(UsernameAlreadyExistException exception, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("usernameAlreadyExistMessage", exception.getMessage());

        return "redirect:" + exception.getRedirectPath();
    }

    @ExceptionHandler(EmailAlreadyExistException.class)
    public String emailAlreadyExistException(RedirectAttributes redirectAttributes, EmailAlreadyExistException exception) {

        String message = exception.getMessage();
        redirectAttributes.addFlashAttribute("emailAlreadyExistMessage", message);

        return "redirect:/profile/" + exception.getUserId() + "/edit-profile";
    }

    @ExceptionHandler(NotificationServiceFeignCallException.class)
    public String handleNotificationFeignCallException(RedirectAttributes redirectAttributes, NotificationServiceFeignCallException exception) {

        String message = exception.getMessage();
        redirectAttributes.addFlashAttribute("notificationServiceFailureMessage", message);

        return "redirect:/notifications";
    }

    @ExceptionHandler(NotValidEmailException.class)
    public String handleNotValidEmailException(RedirectAttributes redirectAttributes, NotValidEmailException exception) {

        String message = exception.getMessage();
        redirectAttributes.addFlashAttribute("notValidEmailMessage", message);

        return "redirect:/notifications";
    }

    @ExceptionHandler(FailedToPostReviewException.class)
    public String handleFailedToPostReviewException(RedirectAttributes redirectAttributes, FailedToPostReviewException exception) {

        String message = exception.getMessage();
        redirectAttributes.addFlashAttribute("failedToPostReviewMessage", message);

        return "redirect:/reviews/" + exception.getUserId() + "/add-review";
    }

    @ExceptionHandler(FailedTopUpException.class)
    public String handleFailedTopUpException(RedirectAttributes redirectAttributes, FailedTopUpException exception) {

        String message = exception.getMessage();
        redirectAttributes.addFlashAttribute("failedTopUpMessage", message);

        return "redirect:/add-funds";
    }

    @ExceptionHandler(FailedPurchaseException.class)
    public String handleFailedTopUpException(RedirectAttributes redirectAttributes, FailedPurchaseException exception) {

        String message = exception.getMessage();
        redirectAttributes.addFlashAttribute("failedPurchaseMessage", message);

        return "redirect:/cart";
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            AccessDeniedException.class,
            NoResourceFoundException.class,
            MethodArgumentTypeMismatchException.class,
            MissingRequestValueException.class
    })
    public ModelAndView handleNotFoundExceptions(Exception exception) {

        return new ModelAndView("not-found");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleAnyException(Exception exception) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("internal-server-error");
        modelAndView.addObject("errorMessage", exception.getClass().getSimpleName());

        return modelAndView;
    }
}
