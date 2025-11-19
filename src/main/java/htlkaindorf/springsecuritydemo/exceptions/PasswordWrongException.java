package htlkaindorf.springsecuritydemo.exceptions;

import org.springframework.security.core.AuthenticationException;

public class PasswordWrongException extends AuthenticationException {
    public PasswordWrongException(String message) {
        super(message);
    }
}
