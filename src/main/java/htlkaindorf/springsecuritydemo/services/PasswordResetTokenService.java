package htlkaindorf.springsecuritydemo.services;

import htlkaindorf.springsecuritydemo.entity.User;

public interface PasswordResetTokenService {

    String generateResetToken(User user);

}
