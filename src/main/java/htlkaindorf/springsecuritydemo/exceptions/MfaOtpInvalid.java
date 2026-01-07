package htlkaindorf.springsecuritydemo.exceptions;

public class MfaOtpInvalid extends RuntimeException {
    public MfaOtpInvalid(String message) {
        super(message);
    }
}
