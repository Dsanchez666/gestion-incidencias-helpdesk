package com.company.backendinc.auth.entra.application.port.out;

import java.time.Instant;
import java.util.Optional;

/**
 * Output port for the interactive Entra session token store.
 */
public interface EntraSessionStorePort {
    void setToken(String accessToken, Instant expiresAt, String refreshToken, String accountHint);

    Optional<String> getValidAccessToken();

    String getAccountHint();

    void clear();
}
