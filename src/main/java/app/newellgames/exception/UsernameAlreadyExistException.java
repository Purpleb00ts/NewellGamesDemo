package app.newellgames.exception;

import lombok.Getter;

@Getter
public class UsernameAlreadyExistException extends RuntimeException {
    private final String redirectPath;

    public UsernameAlreadyExistException(String message, String redirectPath) {
        super(message);
        this.redirectPath = redirectPath;
    }

}
