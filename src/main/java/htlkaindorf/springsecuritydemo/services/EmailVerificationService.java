package htlkaindorf.springsecuritydemo.services;

import htlkaindorf.springsecuritydemo.entity.User;

public interface EmailVerificationService {

    public String generateVerificationToken(User user);

}
