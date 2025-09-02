package app.newellgames.exception;

import lombok.Getter;

import java.util.UUID;
@Getter
public class EmailAlreadyExistException extends RuntimeException {
    private final UUID userId;
    public EmailAlreadyExistException(String message, UUID userId) {
        super(message);
        this.userId = userId;
    }
}
