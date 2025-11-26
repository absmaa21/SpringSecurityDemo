package htlkaindorf.springsecuritydemo.services;

import htlkaindorf.springsecuritydemo.auth.AuthRequest;
import htlkaindorf.springsecuritydemo.auth.AuthResponse;

public interface AuthService {

    AuthResponse login(AuthRequest request);

    void register(AuthRequest request);

}
