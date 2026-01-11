package htlkaindorf.springsecuritydemo.services;

import htlkaindorf.springsecuritydemo.auth.*;

public interface AuthService {

    AuthResponse login(AuthRequest request);

    MfaResponse signin(AuthRequest request);

    AuthResponse verifyOtp(String mfaToken, String otp);

    void register(AuthRequest request);

    void verifyEmail(String token);

    AccessTokenResponse refresh(RefreshTokenRequest request);

}
