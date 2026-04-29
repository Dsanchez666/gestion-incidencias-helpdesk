package com.company.backendinc.auth.application;

import com.company.backendinc.auth.LoginRequest;
import com.company.backendinc.auth.LoginResponse;
import com.company.backendinc.auth.application.port.out.CredentialsVerifierPort;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Application use case that authenticates an operator.
 */
@Service
public class AuthenticateUserUseCase {
    private final CredentialsVerifierPort credentialsVerifier;

    public AuthenticateUserUseCase(CredentialsVerifierPort credentialsVerifier) {
        this.credentialsVerifier = credentialsVerifier;
    }

    public Optional<LoginResponse> authenticate(LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return Optional.empty();
        }
        if (!credentialsVerifier.isValid(request.getUsername(), request.getPassword())) {
            return Optional.empty();
        }
        return Optional.of(new LoginResponse(request.getUsername(), "login ok"));
    }
}
