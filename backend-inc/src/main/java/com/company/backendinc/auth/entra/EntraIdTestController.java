package com.company.backendinc.auth.entra;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/auth/entra")
public class EntraIdTestController {
    private final EntraIdConfigLoader configLoader;
    private final RestTemplate restTemplate = new RestTemplate();

    public EntraIdTestController(EntraIdConfigLoader configLoader) {
        this.configLoader = configLoader;
    }

    @PostMapping("/test")
    public ResponseEntity<EntraLoginResponse> test(@RequestBody EntraLoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest()
                    .body(new EntraLoginResponse(false, null, "Faltan credenciales de usuario o clave."));
        }

        EntraIdConfig config;
        try {
            config = configLoader.load();
        } catch (IOException ex) {
            return ResponseEntity.internalServerError()
                    .body(new EntraLoginResponse(false, null, "No se pudo leer EntraID_Conf.json: " + ex.getMessage()));
        }

        if (config.getClientId() == null || config.getClientId().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new EntraLoginResponse(false, null, "clientId no configurado en EntraID_Conf.json."));
        }

        String tokenUrl = resolveTokenUrl(config);
        if (tokenUrl == null) {
            return ResponseEntity.badRequest()
                    .body(new EntraLoginResponse(false, null, "No se pudo resolver el endpoint de token de Entra ID."));
        }

        String scope = (config.getScope() == null || config.getScope().isBlank())
                ? "openid profile offline_access"
                : config.getScope();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", config.getClientId());
        if (config.getClientSecret() != null && !config.getClientSecret().isBlank()) {
            form.add("client_secret", config.getClientSecret());
        }
        form.add("grant_type", "password");
        form.add("username", request.getUsername());
        form.add("password", request.getPassword());
        form.add("scope", scope);

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

            Object token = response.get("access_token");
            if (token instanceof String tokenValue) {
                return ResponseEntity.ok(new EntraLoginResponse(true, tokenValue, null));
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
