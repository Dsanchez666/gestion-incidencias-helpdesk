package com.company.backendinc.auth.entra;

import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class EntraTokenStore {
    private String accessToken;
    private Instant expiresAt;
    private String refreshToken;
    private String accountHint;

    public synchronized void setToken(String accessToken, Instant expiresAt, String refreshToken, String accountHint) {
        this.accessToken = accessToken;
        this.expiresAt = expiresAt;
        this.refreshToken = refreshToken;
        this.accountHint = accountHint;
    }

    public synchronized Optional<String> getValidAccessToken() {
        if (accessToken == null || accessToken.isBlank()) {
            return Optional.empty();
        }
        if (expiresAt == null || Instant.now().isAfter(expiresAt)) {
            return Optional.empty();
        }
        return Optional.of(accessToken);
    }

    public synchronized String getAccountHint() {
        return accountHint;
    }

    public synchronized void clear() {
        this.accessToken = null;
        this.expiresAt = null;
        this.refreshToken = null;
        this.accountHint = null;
    }
}
