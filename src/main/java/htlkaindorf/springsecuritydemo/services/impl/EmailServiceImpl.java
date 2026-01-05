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

    @Override
    public void sendOtpEmail(String email, String otp) {
        MimeMessage msg = javaMailSender.createMimeMessage();
        
        String htmlContent = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .otp-box { background-color: #f4f4f4; border: 2px solid #007bff; padding: 20px; text-align: center; margin: 20px 0; }
                    .otp-code { font-size: 32px; font-weight: bold; color: #007bff; letter-spacing: 5px; }
                    .warning { color: #dc3545; font-size: 14px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h2>Two-Factor Authentication</h2>
                    <p>Hello,</p>
                    <p>You have requested to sign in. Please use the following One-Time Password (OTP) to complete your authentication:</p>
                    <div class="otp-box">
                        <div class="otp-code">%s</div>
                    </div>
                    <p>This OTP is valid for 5 minutes.</p>
                    <div class="warning">
                        <p><strong>Important:</strong> Do not share this code with anyone. If you did not request this code, please ignore this email.</p>
                    </div>
                    <p>Best regards,<br>SpringSecurityDemo Team</p>
                </div>
            </body>
            </html>
            """, otp);

        try {
            MimeMessageHelper helper = new MimeMessageHelper(msg, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
            helper.setFrom("noreply@google.com");
            helper.setTo(email);
            helper.setSubject("Your OTP Code for Two-Factor Authentication");
            helper.setText(htmlContent, true);
            javaMailSender.send(msg);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }


}
