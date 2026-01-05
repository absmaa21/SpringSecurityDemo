package htlkaindorf.springsecuritydemo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 10;
    private static final int TIME_WINDOW_MINUTES = 5;
    
    private final Map<String, RequestData> requestCache = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // Only apply rate limiting to OTP verification endpoint
        if (!path.equals("/api/auth/otp-signin")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        String key = clientIp + ":" + path;

        RequestData requestData = requestCache.computeIfAbsent(key, k -> new RequestData());

        // Clean up expired entries
        if (requestData.getWindowStart().plusMinutes(TIME_WINDOW_MINUTES).isBefore(LocalDateTime.now())) {
            requestData.reset();
        }

        if (requestData.getRequestCount() >= MAX_REQUESTS) {
            log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);
            response.setStatus(429); // 429 Too Many Requests
            response.getWriter().write("Too many requests. Please try again later.");
            return;
        }

        requestData.incrementCount();
        log.debug("Request count for {}: {}", key, requestData.getRequestCount());
        
        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class RequestData {
        private int requestCount = 0;
        private LocalDateTime windowStart = LocalDateTime.now();

        public void incrementCount() {
            requestCount++;
        }

        public int getRequestCount() {
            return requestCount;
        }

        public LocalDateTime getWindowStart() {
            return windowStart;
        }

        public void reset() {
            requestCount = 0;
            windowStart = LocalDateTime.now();
        }
    }
}
