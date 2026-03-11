package com.company.backendinc.auth.entra;

import java.io.IOException;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/auth/entra")
public class EntraMockTestController {
    private final EntraIdMockConfigLoader mockConfigLoader;
    private final RestTemplate restTemplate = new RestTemplate();

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

        String tokenUrl = resolveTokenUrl(config);
        if (tokenUrl == null) {
            return ResponseEntity.badRequest()
                    .body(new EntraLoginResponse(false, null, "No se pudo resolver el endpoint de token de Entra ID."));
        }

        if (config.getClientId() == null || config.getClientId().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new EntraLoginResponse(false, null, "clientId no configurado en EntraID_Conf.json."));
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", token);
        form.add("client_id", config.getClientId());
        if (config.getClientSecret() != null && !config.getClientSecret().isBlank()) {
            form.add("client_secret", config.getClientSecret());
        }
        if (config.getScope() != null && !config.getScope().isBlank()) {
            form.add("scope", config.getScope());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(tokenUrl, entity, Map.class);
            if (response == null) {
                return ResponseEntity.internalServerError()
                        .body(new EntraLoginResponse(false, null, "Respuesta vacia de Entra ID."));
            }

            Object accessToken = response.get("access_token");
            if (accessToken instanceof String accessTokenValue) {
                if (token.equals(accessTokenValue)) {
                    return ResponseEntity.ok(new EntraLoginResponse(true, accessTokenValue, null));
                }
                return ResponseEntity.status(401)
                        .body(new EntraLoginResponse(false, null, "El token devuelto no coincide con el mocktoken."));
            }

            Object error = response.get("error_description");
            String errorMessage = error != null ? error.toString() : response.toString();
            return ResponseEntity.status(401)
                    .body(new EntraLoginResponse(false, null, errorMessage));
        } catch (RestClientException ex) {
            return ResponseEntity.status(502)
                    .body(new EntraLoginResponse(false, null, "Error comunicando con Entra ID: " + ex.getMessage()));
        }
    }

    private String resolveTokenUrl(EntraIdConfig config) {
        if (config.getAuthorityUrl() != null && !config.getAuthorityUrl().isBlank()) {
            String trimmed = config.getAuthorityUrl().replaceAll("/+$", "");
            if (trimmed.endsWith("/v2.0")) {
                return trimmed.replace("/v2.0", "/oauth2/v2.0/token");
            }
            if (trimmed.contains("/oauth2/v2.0/token")) {
                return trimmed;
            }
        }

        if (config.getTenantId() != null && !config.getTenantId().isBlank()) {
            return "https://login.microsoftonline.com/" + config.getTenantId() + "/oauth2/v2.0/token";
        }

        return null;
    }
}
