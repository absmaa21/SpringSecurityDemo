package htlkaindorf.springsecuritydemo.exceptions;

public class AuthorizationTokenExpiredException extends RuntimeException {
    public AuthorizationTokenExpiredException(String message) {
        super(message);
    }
}
