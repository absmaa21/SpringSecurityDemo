package htlkaindorf.springsecuritydemo.services;

import htlkaindorf.springsecuritydemo.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    String generateToken(User user);

    boolean isTokenValid(String token, UserDetails userDetails);

    String extractUsername(String token);

    String generateMfaToken(User user);

    boolean isMfaToken(String token);

}
