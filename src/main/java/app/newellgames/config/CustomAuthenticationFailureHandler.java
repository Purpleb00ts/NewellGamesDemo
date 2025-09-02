package app.newellgames.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    // Custom handler for authentication failure, so I can display custom error messages
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        String errorMessage;

        if (exception instanceof LockedException) {
            errorMessage = "Your account has been deactivated. Please contact support.";
        } else if (exception instanceof BadCredentialsException) {
            errorMessage = "Invalid username or password.";
        } else {
            errorMessage = "Login failed. Please try again.";
        }

        // Save message in session
        request.getSession().setAttribute("LOGIN_ERROR", errorMessage);

        // Redirect
        response.sendRedirect("/login");
    }
}