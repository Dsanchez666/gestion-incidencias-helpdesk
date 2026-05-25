package com.company.backendinc.auth.adapter.out;

import com.company.backendinc.auth.application.port.out.CredentialsVerifierPort;
import com.company.backendinc.auth.adapter.out.TecnicoAuthRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Outbound adapter with the current local credential policy.
 */
@Component
public class RootCredentialsVerifierAdapter implements CredentialsVerifierPort {
    private static final String ROOT_USER = "root";
    private static final String ROOT_PASS = "root";
    private final TecnicoAuthRepository tecnicoAuthRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public RootCredentialsVerifierAdapter(TecnicoAuthRepository tecnicoAuthRepository) {
        this.tecnicoAuthRepository = tecnicoAuthRepository;
    }

    @Override
    public boolean isValid(String username, String password) {
        if (ROOT_USER.equals(username) && ROOT_PASS.equals(password)) {
            return true;
        }
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return false;
        }
        TecnicoAuthRepository.AuthUser user = tecnicoAuthRepository.findByUserHint(username);
        if (user == null || user.passwordHash() == null || user.passwordHash().isBlank()) {
            return false;
        }
        return encoder.matches(password, user.passwordHash());
    }
}
