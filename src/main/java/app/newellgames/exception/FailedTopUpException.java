package app.newellgames.exception;

import lombok.Getter;

import java.util.UUID;

public class FailedTopUpException extends RuntimeException {

    public FailedTopUpException(String message) {
        super(message);
    }
}
