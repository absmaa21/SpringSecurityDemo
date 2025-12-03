package htlkaindorf.springsecuritydemo.database;

import htlkaindorf.springsecuritydemo.entity.User;
import htlkaindorf.springsecuritydemo.entity.VerificationToken;
import htlkaindorf.springsecuritydemo.repositories.UserRepository;
import htlkaindorf.springsecuritydemo.repositories.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DBPeriodicTasks {

    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;

    @Scheduled(fixedRate = 60 * 1000)
    void cleanupVerificationTokens() {
        List<VerificationToken> expiredTokens = verificationTokenRepository.getVerificationTokensByExpiryDateBefore(LocalDateTime.now());
        verificationTokenRepository.deleteAll(expiredTokens);
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    void cleanupUnverifiedUsers() {
        List<User> unverifiedUsers = userRepository.findUsersByUnverified();
        userRepository.deleteAll(unverifiedUsers);
    }

}
