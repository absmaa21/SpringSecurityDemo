package htlkaindorf.springsecuritydemo.exceptions;

import org.springframework.security.core.AuthenticationException;

public class UsernameWrongException extends AuthenticationException {
    public UsernameWrongException(String message) {
        super(message);
    }
}
