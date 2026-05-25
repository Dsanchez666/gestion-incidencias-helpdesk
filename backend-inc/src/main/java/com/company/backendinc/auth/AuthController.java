package com.company.backendinc.auth;

import com.company.backendinc.auth.application.AuthenticateUserUseCase;
import com.company.backendinc.auth.application.PasswordRecoveryUseCase;
import com.company.backendinc.auth.entra.application.port.out.EntraSessionStorePort;
import java.time.Instant;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final EntraSessionStorePort sessionStore;
    private final PasswordRecoveryUseCase passwordRecoveryUseCase;

    public AuthController(AuthenticateUserUseCase authenticateUserUseCase, EntraSessionStorePort sessionStore, PasswordRecoveryUseCase passwordRecoveryUseCase) {
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.sessionStore = sessionStore;
        this.passwordRecoveryUseCase = passwordRecoveryUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        if (request == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return authenticateUserUseCase.authenticate(request)
                .map(r -> {
                    sessionStore.setToken("basic-session-" + System.currentTimeMillis(),
                            Instant.now().plusSeconds(8 * 3600),
                            null,
                            request.getUsername());
                    return ResponseEntity.ok(r);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/password/recover")
    public ResponseEntity<Void> recoverPassword(@Valid @RequestBody PasswordResetRequest request) {
        passwordRecoveryUseCase.requestReset(request.getUserOrEmail());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request) {
        boolean ok = passwordRecoveryUseCase.confirmReset(request.getToken(), request.getNewPassword());
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
