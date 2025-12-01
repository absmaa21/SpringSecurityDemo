package htlkaindorf.springsecuritydemo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

import javax.naming.InsufficientResourcesException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper mapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> responseBody = new HashMap<>();

        responseBody.put("status", HttpServletResponse.SC_FORBIDDEN);
        responseBody.put("error",  accessDeniedException.getMessage());

        responseBody.put("path", request.getRequestURL());
        mapper.writeValue(response.getWriter(), responseBody);
    }

    private String resolveErrorMessage(Exception ex){
        if (ex instanceof InsufficientResourcesException){
            return "Access denied: authentication is insufficient!";
        } else if (ex instanceof DisabledException) {
            return "Your account is not enabled - verify your email first!";
        } else if (ex instanceof LockedException){
            return "Access denied: user account locked";
        } else if (ex  instanceof CsrfException) {
            return "Access denied: invalid or missing CSRF token.";
        } else if (ex instanceof AccountStatusException){
            return "Access denied: account status invalid.";
        } else{
            return"Forbidden: you do not have the required permission";
        }
    }
}
