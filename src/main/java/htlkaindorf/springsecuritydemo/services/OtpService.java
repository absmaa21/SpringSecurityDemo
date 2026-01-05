package htlkaindorf.springsecuritydemo.services;

public interface OtpService {

    String generateOtp(String username);

    boolean validateOtp(String username, String otp);

    void clearOtp(String username);

}
