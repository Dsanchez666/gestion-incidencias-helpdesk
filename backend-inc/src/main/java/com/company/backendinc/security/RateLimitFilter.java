package com.company.backendinc.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private static final int AUTH_MAX_ATTEMPTS = 5;
    private static final Duration AUTH_WINDOW = Duration.ofMinutes(15);
    private static final int API_MAX_ATTEMPTS = 300;
    private static final Duration API_WINDOW = Duration.ofMinutes(15);

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        String ip = extractClientIp(request);
        boolean isAuthRoute = path.startsWith("/api/auth");
        int maxAttempts = isAuthRoute ? AUTH_MAX_ATTEMPTS : API_MAX_ATTEMPTS;
        Duration window = isAuthRoute ? AUTH_WINDOW : API_WINDOW;
        String key = (isAuthRoute ? "AUTH:" : "API:") + ip;

        Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket(Instant.now().plus(window), 0));
        synchronized (bucket) {
            Instant now = Instant.now();
            if (now.isAfter(bucket.windowEnd)) {
                bucket.windowEnd = now.plus(window);
                bucket.count = 0;
            }
            bucket.count++;
            if (bucket.count > maxAttempts) {
                long retryAfterSeconds = Math.max(1, Duration.between(now, bucket.windowEnd).toSeconds());
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"rate_limited\",\"message\":\"Demasiadas peticiones\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            int comma = forwardedFor.indexOf(',');
            return comma > 0 ? forwardedFor.substring(0, comma).trim() : forwardedFor.trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private static class Bucket {
        private Instant windowEnd;
        private int count;

        private Bucket(Instant windowEnd, int count) {
            this.windowEnd = windowEnd;
            this.count = count;
        }
    }
}

