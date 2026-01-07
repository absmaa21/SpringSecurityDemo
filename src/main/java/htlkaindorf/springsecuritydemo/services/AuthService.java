package htlkaindorf.springsecuritydemo.services;

import htlkaindorf.springsecuritydemo.auth.AuthRequest;
import htlkaindorf.springsecuritydemo.auth.AuthResponse;
import htlkaindorf.springsecuritydemo.auth.MfaRequest;
import htlkaindorf.springsecuritydemo.auth.MfaResponse;

public interface AuthService {

    MfaResponse login(AuthRequest request);

    void register(AuthRequest request);

    void verifyEmail(String token);

    AuthResponse otpLogin(MfaRequest request);

}
