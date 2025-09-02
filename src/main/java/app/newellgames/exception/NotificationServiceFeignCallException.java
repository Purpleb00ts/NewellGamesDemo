package app.newellgames.exception;

public class NotificationServiceFeignCallException extends RuntimeException {

    public NotificationServiceFeignCallException() {
    }

    public NotificationServiceFeignCallException(String message) {
        super(message);
    }
}
