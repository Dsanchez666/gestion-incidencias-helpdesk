package com.company.backendinc.auth.application.port.out;

/**
 * Output port for credential validation policies.
 */
public interface CredentialsVerifierPort {
    boolean isValid(String username, String password);
}
