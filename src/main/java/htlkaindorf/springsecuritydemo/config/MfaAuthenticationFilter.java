package htlkaindorf.springsecuritydemo.config;

import htlkaindorf.springsecuritydemo.services.JwtService;
import htlkaindorf.springsecuritydemo.services.impl.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class MfaAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        
        // Only apply MFA filter to the otp-signin endpoint
        if (!path.equals("/api/auth/otp-signin")) {
            filterChain.doFilter(request, response);
            return;
        }

        log.info("MFA Filter for request: " + request.getRequestURL());
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("No MFA token in request");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "MFA token required");
            return;
        }

        String jwt = authHeader.substring(7);
        
        try {
            // Validate that this is an MFA token
            if (!jwtService.isMfaToken(jwt)) {
                log.warn("Token is not an MFA token");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid MFA token");
                return;
            }

            String username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("MFA token validated for user: " + username);
                } else {
                    log.warn("Invalid MFA token for user: " + username);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid MFA token");
                    return;
                }
            }
        } catch (Exception e) {
            log.error("Error processing MFA token", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid MFA token");
            return;
        }

        log.info("MFA filter finished!");
        filterChain.doFilter(request, response);
    }

}
