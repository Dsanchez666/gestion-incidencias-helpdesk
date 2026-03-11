package com.company.backendinc.auth.entra;

import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/entra")
public class EntraMockTestController {
    private final EntraIdMockConfigLoader mockConfigLoader;

    public EntraMockTestController(EntraIdMockConfigLoader mockConfigLoader) {
        this.mockConfigLoader = mockConfigLoader;
    }

    @PostMapping("/mock-test")
    public ResponseEntity<EntraLoginResponse> mockTest() {
        EntraIdConfig config;
        try {
            config = mockConfigLoader.load();
        } catch (IOException ex) {
            return ResponseEntity.internalServerError()
                    .body(new EntraLoginResponse(false, null, "No se pudo leer EntraID_Conf.json: " + ex.getMessage()));
        }

        String token = config.getMocktoken();
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new EntraLoginResponse(false, null, "mocktoken no configurado en EntraID_Conf.json."));
        }

        if ("REEMPLAZAR_TOKEN".equals(token)) {
            return ResponseEntity.badRequest()
                    .body(new EntraLoginResponse(false, null, "mocktoken pendiente de configurar."));
        }

        return ResponseEntity.ok(new EntraLoginResponse(true, token, null));
    }
}
