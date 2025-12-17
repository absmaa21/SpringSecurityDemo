package htlkaindorf.springsecuritydemo.exceptions;

public class PasswordResetTokenExpired extends RuntimeException {
    public PasswordResetTokenExpired(String message) {
        super(message);
    }
}
