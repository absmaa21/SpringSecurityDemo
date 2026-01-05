package htlkaindorf.springsecuritydemo.services;

import htlkaindorf.springsecuritydemo.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    String generateToken(User user);

    String generateMfaToken(User user);

    boolean isTokenValid(String token, UserDetails userDetails);

    boolean isMfaToken(String token);

    String extractUsername(String token);

}
