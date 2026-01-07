package htlkaindorf.springsecuritydemo.exceptions;

public class MfaTokenInvalid extends RuntimeException {
    public MfaTokenInvalid(String message) {
        super(message);
    }
}
