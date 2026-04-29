package com.company.backendinc.auth.entra.application.port.out;

/**
 * Result returned by remote Entra token exchanges.
 */
public record EntraTokenResult(
        boolean success,
        String accessToken,
        String errorMessage,
        Integer expiresInSeconds,
        String refreshToken) {
}
