package htlkaindorf.springsecuritydemo.services.impl;

import htlkaindorf.springsecuritydemo.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Override
    public void sendVerificationEmail(String email, String token) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("noreply@google.com");
        msg.setTo(email);
        msg.setSubject("Email Verification");
        msg.setText(token);
        javaMailSender.send(msg);
    }

}
