package com.company.backendinc.auth.entra.application;

/**
 * Generic application result with explicit HTTP status mapping.
 */
public record UseCaseResult<T>(int status, T body) {
}
