package htlkaindorf.springsecuritydemo.exceptions;

import org.springframework.security.core.AuthenticationException;

public class UserAlreadyExistsAuthenticationException extends AuthenticationException {
  public UserAlreadyExistsAuthenticationException(String message) {super(message);}
}
