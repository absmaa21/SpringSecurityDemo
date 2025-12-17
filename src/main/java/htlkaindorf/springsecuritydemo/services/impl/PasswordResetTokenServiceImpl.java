package htlkaindorf.springsecuritydemo.services.impl;

import htlkaindorf.springsecuritydemo.entity.ResetToken;
import htlkaindorf.springsecuritydemo.entity.User;
import htlkaindorf.springsecuritydemo.repositories.ResetTokenRepository;
import htlkaindorf.springsecuritydemo.services.PasswordResetTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    private final ResetTokenRepository resetTokenRepository;

    @Value("${application.forgot-pwd.expiration}")
    private Long tokenExpirationMs;

    @Override
    public String generateResetToken(User user) {
        ResetToken resetToken = new ResetToken();
        resetToken.setToken(UUID.randomUUID().toString());
        resetToken.setExpiryDate(LocalDateTime.now().plusSeconds(tokenExpirationMs / 1000));
        resetToken.setUser(user);

        resetTokenRepository.save(resetToken);

        return resetToken.getToken();
    }

}
