package com.company.backendinc.auth.entra;

import com.company.backendinc.auth.entra.application.port.out.EntraSessionStorePort;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class EntraTokenStore implements EntraSessionStorePort {
    private String accessToken;
    private Instant expiresAt;
    private String refreshToken;
    private String accountHint;

    @Override
    public synchronized void setToken(String accessToken, Instant expiresAt, String refreshToken, String accountHint) {
        this.accessToken = accessToken;
        this.expiresAt = expiresAt;
        this.refreshToken = refreshToken;
        this.accountHint = accountHint;
    }

    @Override
    public synchronized Optional<String> getValidAccessToken() {
        if (accessToken == null || accessToken.isBlank()) {
            return Optional.empty();
        }
        if (expiresAt == null || Instant.now().isAfter(expiresAt)) {
            return Optional.empty();
        }
        return Optional.of(accessToken);
    }

    @Override
    public synchronized String getAccountHint() {
        return accountHint;
    }

    @Override
    public synchronized void clear() {
        this.accessToken = null;
        this.expiresAt = null;
        this.refreshToken = null;
        this.accountHint = null;
    }
}
