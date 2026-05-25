package com.company.backendinc.auth.entra.application;

import com.company.backendinc.auth.entra.EntraIdConfig;
import com.company.backendinc.auth.entra.EntraLoginRequest;
import com.company.backendinc.auth.entra.EntraLoginResponse;
import com.company.backendinc.auth.entra.application.port.out.EntraConfigurationPort;
import com.company.backendinc.auth.entra.application.port.out.EntraSessionStorePort;
import com.company.backendinc.auth.entra.application.port.out.EntraTokenGatewayPort;
import com.company.backendinc.auth.entra.application.port.out.EntraTokenResult;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Application use case that orchestrates the Entra login and token flows.
 */
@Service
public class EntraAuthenticationUseCase {
    private static final Logger log = LoggerFactory.getLogger(EntraAuthenticationUseCase.class);
    private final EntraConfigurationPort configurationPort;
    private final EntraTokenGatewayPort tokenGatewayPort;
    private final EntraSessionStorePort sessionStorePort;
    private String lastState;

    public EntraAuthenticationUseCase(@Qualifier("entraIdConfigLoader") EntraConfigurationPort configurationPort,
            EntraTokenGatewayPort tokenGatewayPort,
            EntraSessionStorePort sessionStorePort) {
        this.configurationPort = configurationPort;
        this.tokenGatewayPort = tokenGatewayPort;
        this.sessionStorePort = sessionStorePort;
    }

    public UseCaseResult<EntraLoginResponse> appToken() {
        EntraIdConfig config;
        try {
            config = configurationPort.load();
        } catch (IOException ex) {
            log.error("Entra app-token: error leyendo EntraID_Conf.json", ex);
            return new UseCaseResult<>(500,
                    new EntraLoginResponse(false, null, "No se pudo leer EntraID_Conf.json: " + ex.getMessage()));
        }

        if (!hasCredentials(config)) {
            return new UseCaseResult<>(400,
                    new EntraLoginResponse(false, null, "clientId/clientSecret no configurados en EntraID_Conf.json."));
        }

        EntraTokenResult result = tokenGatewayPort.requestClientCredentialsToken(config);
        return toLoginResponse(result);
    }

    public UseCaseResult<EntraLoginResponse> testCredentials(EntraLoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return new UseCaseResult<>(400, new EntraLoginResponse(false, null, "Faltan credenciales de usuario o clave."));
        }

        EntraIdConfig config;
        try {
            config = configurationPort.load();
        } catch (IOException ex) {
            return new UseCaseResult<>(500,
                    new EntraLoginResponse(false, null, "No se pudo leer EntraID_Conf.json: " + ex.getMessage()));
        }

        if (config.getClientId() == null || config.getClientId().isBlank()) {
            return new UseCaseResult<>(400,
                    new EntraLoginResponse(false, null, "clientId no configurado en EntraID_Conf.json."));
        }

        EntraTokenResult result = tokenGatewayPort.requestPasswordToken(config, request.getUsername(), request.getPassword());
        if (result.success() && result.accessToken() != null && !result.accessToken().isBlank()) {
            int expiresIn = result.expiresInSeconds() != null ? result.expiresInSeconds() : 3600;
            Instant expiresAt = Instant.now().plusSeconds(Math.max(expiresIn - 30L, 30L));
            sessionStorePort.setToken(result.accessToken(), expiresAt, result.refreshToken(), request.getUsername());
            log.info("Entra password-flow: token de usuario almacenado para {}", request.getUsername());
        }
        return toLoginResponse(result);
    }

    public UseCaseResult<String> loginRedirect() {
        EntraIdConfig config;
        try {
            config = configurationPort.load();
        } catch (IOException ex) {
            log.error("Entra login: error leyendo EntraID_Conf.json", ex);
            return new UseCaseResult<>(500, null);
        }

        String authorizeUrl = resolveAuthorizeUrl(config);
        if (authorizeUrl == null) {
            return new UseCaseResult<>(400, null);
        }

        String scope = resolveInteractiveScope(config);
        String redirectUri = resolveRedirectUri(config);
        String state = "st" + System.currentTimeMillis();
        this.lastState = state;

        String url = authorizeUrl
                + "?client_id=" + urlEncode(config.getClientId())
                + "&response_type=code"
                + "&redirect_uri=" + urlEncode(redirectUri)
                + "&response_mode=query"
                + "&scope=" + urlEncode(scope)
                + "&state=" + urlEncode(state)
                + "&prompt=select_account";

        log.info("Entra login: redirect to {}", url);
        return new UseCaseResult<>(302, url);
    }

    public UseCaseResult<String> callback(String code, String state, String error, String errorDescription) {
        if (error != null && !error.isBlank()) {
            log.warn("Entra callback error: {} {}", error, errorDescription);
            return new UseCaseResult<>(401, renderCallbackPage(false, "Error de login: " + error));
        }

        if (code == null || code.isBlank()) {
            return new UseCaseResult<>(400, renderCallbackPage(false, "No se recibio el codigo de autorizacion."));
        }

        if (lastState != null && state != null && !lastState.equals(state)) {
            return new UseCaseResult<>(401, renderCallbackPage(false, "Estado invalido."));
        }

        EntraIdConfig config;
        try {
            config = configurationPort.load();
        } catch (IOException ex) {
            log.error("Entra callback: error leyendo EntraID_Conf.json", ex);
            return new UseCaseResult<>(500, renderCallbackPage(false, "Error leyendo EntraID_Conf.json."));
        }

        EntraTokenResult tokenResult = tokenGatewayPort.exchangeAuthorizationCode(config, code);
        if (!tokenResult.success()) {
            return new UseCaseResult<>(401, renderCallbackPage(false, "Error obteniendo token: " + tokenResult.errorMessage()));
        }

        int expiresIn = tokenResult.expiresInSeconds() != null ? tokenResult.expiresInSeconds() : 3600;
        Instant expiresAt = Instant.now().plusSeconds(Math.max(expiresIn - 30L, 30L));
        sessionStorePort.setToken(tokenResult.accessToken(), expiresAt, tokenResult.refreshToken(), null);
        log.info("Entra callback: token almacenado (expiresIn={}s)", expiresIn);

        return new UseCaseResult<>(200, renderCallbackPage(true, "Login correcto. Redirigiendo a la app..."));
    }

    public Map<String, Object> status() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("loggedIn", sessionStorePort.getValidAccessToken().isPresent());
        response.put("account", sessionStorePort.getAccountHint());
        return response;
    }

    private boolean hasCredentials(EntraIdConfig config) {
        return config.getClientId() != null && !config.getClientId().isBlank()
                && config.getClientSecret() != null && !config.getClientSecret().isBlank();
    }

    private UseCaseResult<EntraLoginResponse> toLoginResponse(EntraTokenResult result) {
        if (result.success()) {
            return new UseCaseResult<>(200, new EntraLoginResponse(true, result.accessToken(), null));
        }
        int status = result.errorMessage() != null && result.errorMessage().contains("comunicando") ? 502 : 401;
        if (result.errorMessage() != null && result.errorMessage().contains("No se pudo resolver")) {
            status = 400;
        }
        return new UseCaseResult<>(status, new EntraLoginResponse(false, null, result.errorMessage()));
    }

    private String resolveAuthorizeUrl(EntraIdConfig config) {
        if (config.getAuthorityUrl() != null && !config.getAuthorityUrl().isBlank()) {
            String trimmed = config.getAuthorityUrl().replaceAll("/+$", "");
            if (trimmed.endsWith("/v2.0")) {
                return trimmed.replace("/v2.0", "/oauth2/v2.0/authorize");
            }
            if (trimmed.contains("/oauth2/v2.0/authorize")) {
                return trimmed;
            }
        }
        if (config.getTenantId() != null && !config.getTenantId().isBlank()) {
            return "https://login.microsoftonline.com/" + config.getTenantId() + "/oauth2/v2.0/authorize";
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

    private String urlEncode(String value) {
        return java.net.URLEncoder.encode(value == null ? "" : value, java.nio.charset.StandardCharsets.UTF_8);
    }

    private String renderCallbackPage(boolean success, String message) {
        String title = success ? "Login correcto" : "Login fallido";
        String statusClass = success ? "ok" : "error";
        String template = """
                <!doctype html>
                <html lang="es">
                <head>
                  <meta charset="utf-8">
                  <title>%s</title>
                  <style>
                    body { font-family: Arial, sans-serif; padding: 24px; }
                    .ok { color: #137333; }
                    .error { color: #b00020; }
                    .box { border: 1px solid #ddd; padding: 16px; max-width: 560px; }
                  </style>
                </head>
                <body>
                  <div class="box">
                    <h2 class="%s">%s</h2>
                    <p>%s</p>
                    <p>Redirección automática: <a href="http://localhost:3000/startup">Abrir app</a></p>
                  </div>
                  <script>
                    window.location.href = "http://localhost:3000/startup";
                  </script>
                </body>
                </html>
                """;
        return String.format(template, title, statusClass, title, message);
    }
}
