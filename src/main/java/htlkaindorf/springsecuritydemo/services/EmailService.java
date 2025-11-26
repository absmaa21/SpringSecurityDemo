package htlkaindorf.springsecuritydemo.services;

public interface EmailService {

    public void sendVerificationEmail(String email, String token);

}
