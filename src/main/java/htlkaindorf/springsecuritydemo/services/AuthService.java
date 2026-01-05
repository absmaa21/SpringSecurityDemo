package htlkaindorf.springsecuritydemo.services;

import htlkaindorf.springsecuritydemo.auth.AuthRequest;
import htlkaindorf.springsecuritydemo.auth.AuthResponse;

public interface AuthService {

    AuthResponse login(AuthRequest request);

    AuthResponse signin(AuthRequest request);

    AuthResponse verifyOtp(String mfaToken, String otp);

    void register(AuthRequest request);

    void verifyEmail(String token);

}
