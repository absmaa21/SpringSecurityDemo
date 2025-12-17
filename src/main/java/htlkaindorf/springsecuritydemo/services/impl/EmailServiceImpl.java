package htlkaindorf.springsecuritydemo.services.impl;

import htlkaindorf.springsecuritydemo.services.EmailService;
import htlkaindorf.springsecuritydemo.services.EmailTemplateService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final EmailTemplateService emailTemplateService;

    @Override
    public void sendVerificationEmail(String email, String token) {

        MimeMessage msg = javaMailSender.createMimeMessage();
        String verificationUrl = "http://localhost:8080/api/auth/verify-email?token=" + token;
        String htmlContent = emailTemplateService.buildVerificationEmail(email, verificationUrl);

        try {
            MimeMessageHelper helper = new MimeMessageHelper(msg, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
            helper.setFrom("noreply@google.com");
            helper.setTo(email);
            helper.setSubject("Email Verification");
            helper.setText(htmlContent, true);
            javaMailSender.send(msg);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void sendResetEmail(String email, String token) {

        MimeMessage msg = javaMailSender.createMimeMessage();
        String resetPasswordUrl = "http://localhost:8080/api/auth/reset-pw?token=" + token;
        String htmlContent = emailTemplateService.buildResetPw(email, resetPasswordUrl);

        try {
            MimeMessageHelper helper = new MimeMessageHelper(msg, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
            helper.setFrom("noreply@google.com");
            helper.setTo(email);
            helper.setSubject("Password Reset");
            helper.setText(htmlContent, true);
            javaMailSender.send(msg);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }


}
