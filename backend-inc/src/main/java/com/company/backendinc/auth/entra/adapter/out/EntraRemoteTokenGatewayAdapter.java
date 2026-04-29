package com.company.backendinc.auth.entra.adapter.out;

import com.company.backendinc.auth.entra.EntraIdConfig;
import com.company.backendinc.auth.entra.application.port.out.EntraTokenGatewayPort;
import com.company.backendinc.auth.entra.application.port.out.EntraTokenResult;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Outbound adapter that talks to the Entra token endpoint.
 */
@Component
public class EntraRemoteTokenGatewayAdapter implements EntraTokenGatewayPort {
    private static final Logger log = LoggerFactory.getLogger(EntraRemoteTokenGatewayAdapter.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public EntraTokenResult requestClientCredentialsToken(EntraIdConfig config) {
        String tokenUrl = resolveTokenUrl(config);
        if (tokenUrl == null) {
            return new EntraTokenResult(false, null, "No se pudo resolver el endpoint de token de Entra ID.", null, null);
        }

        String scope = (config.getScope() == null || config.getScope().isBlank())
                ? "https://graph.microsoft.com/.default"
                : config.getScope();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", config.getClientId());
        form.add("client_secret", config.getClientSecret());
        form.add("grant_type", "client_credentials");
        form.add("scope", scope);

        return postForm(tokenUrl, form);
    }

    @Override
    public EntraTokenResult requestPasswordToken(EntraIdConfig config, String username, String password) {
        String tokenUrl = resolveTokenUrl(config);
        if (tokenUrl == null) {
            return new EntraTokenResult(false, null, "No se pudo resolver el endpoint de token de Entra ID.", null, null);
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
        form.add("username", username);
        form.add("password", password);
        form.add("scope", scope);

        return postForm(tokenUrl, form);
    }

    @Override
    public EntraTokenResult exchangeAuthorizationCode(EntraIdConfig config, String code) {
        String tokenUrl = resolveTokenUrl(config);
        if (tokenUrl == null) {
            return new EntraTokenResult(false, null, "No se pudo resolver el endpoint de token de Entra ID.", null, null);
        }

        String redirectUri = resolveRedirectUri(config);
        String scope = resolveInteractiveScope(config);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", config.getClientId());
        if (config.getClientSecret() != null && !config.getClientSecret().isBlank()) {
            form.add("client_secret", config.getClientSecret());
        }
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", redirectUri);
        form.add("scope", scope);

        return postForm(tokenUrl, form);
    }

    private EntraTokenResult postForm(String tokenUrl, MultiValueMap<String, String> form) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(tokenUrl, entity, Map.class);
            if (response == null) {
                return new EntraTokenResult(false, null, "Respuesta vacia de Entra ID.", null, null);
            }

            Object token = response.get("access_token");
            if (token instanceof String tokenValue) {
                Integer expiresIn = response.get("expires_in") instanceof Number number ? number.intValue() : null;
                String refreshToken = response.get("refresh_token") instanceof String refresh ? refresh : null;
                return new EntraTokenResult(true, tokenValue, null, expiresIn, refreshToken);
            }

            Object error = response.get("error_description");
            String errorMessage = error != null ? error.toString() : response.toString();
            return new EntraTokenResult(false, null, errorMessage, null, null);
        } catch (RestClientException ex) {
            log.error("Entra token gateway error", ex);
            return new EntraTokenResult(false, null, "Error comunicando con Entra ID: " + ex.getMessage(), null, null);
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

    private String resolveInteractiveScope(EntraIdConfig config) {
        if (config.getScope() == null || config.getScope().isBlank() || config.getScope().contains(".default")) {
            return "openid profile offline_access User.Read Mail.Read";
        }
        return config.getScope();
    }

    private String resolveRedirectUri(EntraIdConfig config) {
        if (config.getRedirectUri() != null && !config.getRedirectUri().isBlank()) {
            return config.getRedirectUri();
        }
        return "http://localhost:4000/api/auth/entra/callback";
    }
}
