package com.company.backendinc.auth.adapter.out;

import com.company.backendinc.auth.application.port.out.CredentialsVerifierPort;
import org.springframework.stereotype.Component;

/**
 * Outbound adapter with the current local credential policy.
 */
@Component
public class RootCredentialsVerifierAdapter implements CredentialsVerifierPort {
    private static final String ROOT_USER = "root";
    private static final String ROOT_PASS = "root";

    @Override
    public boolean isValid(String username, String password) {
        return ROOT_USER.equals(username) && ROOT_PASS.equals(password);
    }
}
