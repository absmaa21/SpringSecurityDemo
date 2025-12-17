package htlkaindorf.springsecuritydemo.controller;

import htlkaindorf.springsecuritydemo.entity.ResetToken;
import htlkaindorf.springsecuritydemo.entity.User;
import htlkaindorf.springsecuritydemo.exceptions.PasswordResetTokenExpired;
import htlkaindorf.springsecuritydemo.repositories.ResetTokenRepository;
import htlkaindorf.springsecuritydemo.repositories.UserRepository;
import htlkaindorf.springsecuritydemo.services.EmailService;
import htlkaindorf.springsecuritydemo.services.PasswordResetTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordController {

    private final UserRepository userRepository;
    private final PasswordResetTokenService passwordResetTokenService;
    private final EmailService emailService;
    private final ResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;


    @PostMapping("/forgot-pwd")
    public void forgotPassword(
            @RequestBody String email
    ) {
        Optional<User> user = userRepository.findUserByUsername(email);

        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User with email/username " + email + " not found!");
        }

        String token = passwordResetTokenService.generateResetToken(user.get());
        emailService.sendResetEmail(email, token);
    }


    @PostMapping("/reset-pw")
    public void resetPassword(
            @RequestParam String token,
            @RequestBody String password
    ) {
        ResetToken resetToken = resetTokenRepository.findResetTokenByToken(token);
        if (resetToken == null) {
            throw new PasswordResetTokenExpired("Reset Token invalid!");
        }

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new PasswordResetTokenExpired("Password Reset Token is expired!");
        }

        if (password == null) {
            throw new RequestRejectedException("Body is wrong");
        }

        resetToken.getUser().setPassword(passwordEncoder.encode(password));
        resetTokenRepository.delete(resetToken);
    }

}
