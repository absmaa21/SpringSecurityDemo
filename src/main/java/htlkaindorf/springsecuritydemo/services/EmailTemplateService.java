package htlkaindorf.springsecuritydemo.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    private final TemplateEngine templateEngine;

    public String buildVerificationEmail(String email, String verificationUrl) {
        Context context = new Context();
        context.setVariable("name", email.split("@")[0]);
        context.setVariable("verificationUrl", verificationUrl);
        return templateEngine.process("/verify-account", context);
    }

}
