package htlkaindorf.springsecuritydemo.exceptions;

public class InvalidMfaTokenException extends RuntimeException {
    public InvalidMfaTokenException(String message) {
        super(message);
    }
}
