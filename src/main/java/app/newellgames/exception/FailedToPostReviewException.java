package app.newellgames.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class FailedToPostReviewException extends RuntimeException {
    private final UUID userId;
    public FailedToPostReviewException(String message, UUID userId) {
        super(message);
        this.userId = userId;
    }
}
