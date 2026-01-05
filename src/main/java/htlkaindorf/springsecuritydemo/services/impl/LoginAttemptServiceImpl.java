package htlkaindorf.springsecuritydemo.services.impl;

import htlkaindorf.springsecuritydemo.services.LoginAttemptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;

    private final Map<String, AttemptData> attemptsCache = new ConcurrentHashMap<>();

    @Override
    public void loginSucceeded(String username) {
        attemptsCache.remove(username);
        log.info("Login succeeded for user: {}", username);
    }

    @Override
    public void loginFailed(String username) {
        AttemptData attempts = attemptsCache.getOrDefault(username, new AttemptData());
        attempts.incrementAttempts();
        attemptsCache.put(username, attempts);
        
        log.warn("Login failed for user: {}. Total failed attempts: {}", 
                username, attempts.getAttempts());
        
        if (attempts.getAttempts() >= MAX_ATTEMPTS) {
            log.error("Account locked for user: {} due to {} failed login attempts", 
                    username, attempts.getAttempts());
        }
    }

    @Override
    public boolean isBlocked(String username) {
        AttemptData attempts = attemptsCache.get(username);
        
        if (attempts == null) {
            return false;
        }
        
        // Check if lockout period has expired
        if (attempts.getAttempts() >= MAX_ATTEMPTS) {
            if (attempts.getLastAttemptTime().plusMinutes(LOCKOUT_DURATION_MINUTES).isBefore(LocalDateTime.now())) {
                // Lockout period expired, reset attempts
                attemptsCache.remove(username);
                log.info("Lockout period expired for user: {}", username);
                return false;
            }
            return true;
        }
        
        return false;
    }

    private static class AttemptData {
        private int attempts = 0;
        private LocalDateTime lastAttemptTime = LocalDateTime.now();

        public void incrementAttempts() {
            this.attempts++;
            this.lastAttemptTime = LocalDateTime.now();
        }

        public int getAttempts() {
            return attempts;
        }

        public LocalDateTime getLastAttemptTime() {
            return lastAttemptTime;
        }
    }
}
