package com.company.backendinc.auth.application;

import com.company.backendinc.auth.adapter.out.TecnicoAuthRepository;
import com.company.backendinc.inbox.adapter.out.TecnicoNotificationGateway;
import java.time.Instant;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordRecoveryUseCase {
    private final TecnicoAuthRepository tecnicoAuthRepository;
    private final TecnicoNotificationGateway notificationGateway;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public PasswordRecoveryUseCase(TecnicoAuthRepository tecnicoAuthRepository, TecnicoNotificationGateway notificationGateway) {
        this.tecnicoAuthRepository = tecnicoAuthRepository;
        this.notificationGateway = notificationGateway;
    }

    public void requestReset(String userOrEmail) {
        TecnicoAuthRepository.AuthUser user = tecnicoAuthRepository.findByUserHint(userOrEmail);
        if (user == null) return;
        String token = tecnicoAuthRepository.createResetToken(user.id(), Instant.now().plusSeconds(3600));
        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        notificationGateway.sendEmail(new TecnicoNotificationGateway.EmailRequest(
                user.email(),
                "Recuperación de contraseña - ETNA",
                "Hola " + user.nombre() + ",\n\nPara restablecer tu contraseña accede al siguiente enlace:\n" + resetLink + "\n\nVálido por 1 hora.",
                null,
                null));
    }

    public boolean confirmReset(String token, String newPassword) {
        Long tecnicoId = tecnicoAuthRepository.consumeResetToken(token);
        if (tecnicoId == null) return false;
        String hash = encoder.encode(newPassword);
        tecnicoAuthRepository.updatePasswordHash(tecnicoId, hash);
        return true;
    }
}

