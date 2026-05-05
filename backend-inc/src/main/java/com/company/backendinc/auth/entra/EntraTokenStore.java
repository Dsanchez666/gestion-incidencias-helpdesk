package com.company.backendinc.auth.entra;

import com.company.backendinc.auth.entra.application.port.out.EntraSessionStorePort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Base64;
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
        this.accountHint = (accountHint == null || accountHint.isBlank()) ? extractAccountHint(accessToken) : accountHint;
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

    private String extractAccountHint(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonNode json = new ObjectMapper().readTree(payload);
            if (json.hasNonNull("preferred_username")) {
                return json.get("preferred_username").asText();
            }
            if (json.hasNonNull("upn")) {
                return json.get("upn").asText();
            }
            if (json.hasNonNull("email")) {
                return json.get("email").asText();
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }
}
