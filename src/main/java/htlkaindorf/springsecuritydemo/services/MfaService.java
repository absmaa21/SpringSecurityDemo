package htlkaindorf.springsecuritydemo.services;

import htlkaindorf.springsecuritydemo.entity.User;

public interface MfaService {

    String generateMfaToken(User user);

}
