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
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        log.info("JWT Filter for request: " + request.getRequestURL());
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("No token in request: signin, signup, public, ...");
            filterChain.doFilter(request, response);
            // Ab hier würde der response gehandelt werden
            return;
        }

        String jwt = authHeader.substring(7);

        if (!jwtService.isAccessToken(jwt)) {
            log.info("Token given but not of type access!");
            filterChain.doFilter(request, response);
            return;
        }

        String username = jwtService.extractUsername(jwt);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                // Setzt paar sicherheits dingens für die Tokens
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.info("End of filter - Authentication = {}", authToken != null ? authToken.getName() : "null");
                log.info("Jwt Authentication Object created!");
            } else {
                log.warn("Invalid Jwt token for user: " + username);
                // Hiermit wird die Chain ganz beendet
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Jwt token");
                return;
            }

        }

        log.info("Jwt filter finished!");
        filterChain.doFilter(request, response);
    }

}
