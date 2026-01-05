package htlkaindorf.springsecuritydemo.services.impl;

import htlkaindorf.springsecuritydemo.services.OtpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private static final int OTP_EXPIRATION_MINUTES = 5;

    @Override
    public String generateOtp(String username) {
        // Generate 6-digit OTP
        int otp = 100000 + random.nextInt(900000);
        String otpString = String.valueOf(otp);
        
        // Store OTP with expiration time
        OtpData otpData = new OtpData(otpString, LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES));
        otpStorage.put(username, otpData);
        
        log.info("Generated OTP for user: {}", username);
        return otpString;
    }

    @Override
    public boolean validateOtp(String username, String otp) {
        OtpData otpData = otpStorage.get(username);
        
        if (otpData == null) {
            log.warn("No OTP found for user: {}", username);
            return false;
        }
        
        if (otpData.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("OTP expired for user: {}", username);
            otpStorage.remove(username);
            return false;
        }
        
        boolean isValid = otpData.getOtp().equals(otp);
        if (isValid) {
            log.info("OTP validated successfully for user: {}", username);
        } else {
            log.warn("Invalid OTP for user: {}", username);
        }
        
        return isValid;
    }

    @Override
    public void clearOtp(String username) {
        otpStorage.remove(username);
        log.info("Cleared OTP for user: {}", username);
    }

    private static class OtpData {
        private final String otp;
        private final LocalDateTime expiryDate;

        public OtpData(String otp, LocalDateTime expiryDate) {
            this.otp = otp;
            this.expiryDate = expiryDate;
        }

        public String getOtp() {
            return otp;
        }

        public LocalDateTime getExpiryDate() {
            return expiryDate;
        }
    }
}
