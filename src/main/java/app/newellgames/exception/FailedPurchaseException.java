package app.newellgames.exception;

public class FailedPurchaseException extends RuntimeException {
    public FailedPurchaseException(String message) {
        super(message);
    }
}
