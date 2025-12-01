package htlkaindorf.springsecuritydemo.database;

import htlkaindorf.springsecuritydemo.entity.VerificationToken;
import htlkaindorf.springsecuritydemo.repositories.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DBChecker {

    private final VerificationTokenRepository verificationTokenRepository;

    @Scheduled(fixedRate = 60000)
    public void cleanupVerificationTokens() {
        List<VerificationToken> expiredTokens = verificationTokenRepository.getVerificationTokensByExpiryDateBefore(LocalDateTime.now());
        verificationTokenRepository.deleteAll(expiredTokens);
    }

}
