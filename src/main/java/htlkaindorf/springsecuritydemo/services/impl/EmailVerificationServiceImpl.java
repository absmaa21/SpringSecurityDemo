package htlkaindorf.springsecuritydemo.services.impl;

import htlkaindorf.springsecuritydemo.entity.User;
import htlkaindorf.springsecuritydemo.entity.VerificationToken;
import htlkaindorf.springsecuritydemo.repositories.VerificationTokenRepository;
import htlkaindorf.springsecuritydemo.services.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final VerificationTokenRepository verificationTokenRepository;

    @Value("${application.security.verify.expiration}")
    private long tokenExpiration;

    @Override
    public String generateVerificationToken(User user) {
        VerificationToken newToken = new VerificationToken();
        newToken.setToken(String.valueOf(UUID.randomUUID()));
        newToken.setExpiryDate(LocalDateTime.now().plusSeconds(tokenExpiration / 1000));
        newToken.setUser(user);

        verificationTokenRepository.save(newToken);

        return newToken.getToken();
    }

}
