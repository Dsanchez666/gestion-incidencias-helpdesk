package com.company.backendinc.auth.entra;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/auth/entra")
public class EntraIdTestController {
    private static final Logger log = LoggerFactory.getLogger(EntraIdTestController.class);
    private final EntraIdConfigLoader configLoader;
    private final EntraTokenStore tokenStore;
    private final RestTemplate restTemplate = new RestTemplate();
    private String lastState;

    public EntraIdTestController(EntraIdConfigLoader configLoader, EntraTokenStore tokenStore) {
        this.configLoader = configLoader;
        this.tokenStore = tokenStore;
    }

    @PostMapping(value = "/test", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntraLoginResponse> test(@RequestBody EntraLoginRequest request) {
        return testInternal(request);
    }

    @PostMapping(value = "/test", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<EntraLoginResponse> testForm(@ModelAttribute EntraLoginRequest request) {
        return testInternal(request);
    }

    @PostMapping("/app-token")
    public ResponseEntity<EntraLoginResponse> appToken() {
        log.info("Entra app-token: solicitud recibida");
        EntraIdConfig config;
        try {
            config = configLoader.load();
        } catch (IOException ex) {
            log.error("Entra app-token: error leyendo EntraID_Conf.json", ex);
            return ResponseEntity.internalServerError()
                    .body(new EntraLoginResponse(false, null, "No se pudo leer EntraID_Conf.json: " + ex.getMessage()));
        }

        if (config.getClientId() == null || config.getClientId().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new EntraLoginResponse(false, null, "clientId no configurado en EntraID_Conf.json."));
        }
        if (config.getClientSecret() == null || config.getClientSecret().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new EntraLoginResponse(false, null, "clientSecret no configurado en EntraID_Conf.json."));
        }

        String tokenUrl = resolveTokenUrl(config);
        if (tokenUrl == null) {
            return ResponseEntity.badRequest()
                    .body(new EntraLoginResponse(false, null, "No se pudo resolver el endpoint de token de Entra ID."));
        }

        String scope = (config.getScope() == null || config.getScope().isBlank())
                ? "https://graph.microsoft.com/.default"
                : config.getScope();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", config.getClientId());
        form.add("client_secret", config.getClientSecret());
        form.add("grant_type", "client_credentials");
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
                log.info("Entra app-token: token recibido (len={})", tokenValue.length());
                return ResponseEntity.ok(new EntraLoginResponse(true, tokenValue, null));
            }

            Object error = response.get("error_description");
            String errorMessage = error != null ? error.toString() : response.toString();
            log.warn("Entra app-token: respuesta con error {}", errorMessage);
            return ResponseEntity.status(401)
                    .body(new EntraLoginResponse(false, null, errorMessage));
        } catch (RestClientException ex) {
            log.error("Entra app-token: error comunicando con Entra ID", ex);
            return ResponseEntity.status(502)
                    .body(new EntraLoginResponse(false, null, "Error comunicando con Entra ID: " + ex.getMessage()));
        }
    }

    @GetMapping("/login")
    public ResponseEntity<Void> login() {
        EntraIdConfig config;
        try {
            config = configLoader.load();
        } catch (IOException ex) {
            log.error("Entra login: error leyendo EntraID_Conf.json", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        String authorizeUrl = resolveAuthorizeUrl(config);
        if (authorizeUrl == null) {
            return ResponseEntity.badRequest().build();
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
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, url)
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callback(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription) {
        if (error != null && !error.isBlank()) {
            log.warn("Entra callback error: {} {}", error, errorDescription);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.TEXT_HTML)
                    .body(renderCallbackPage(false, "Error de login: " + error));
        }

        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_HTML)
                    .body(renderCallbackPage(false, "No se recibio el codigo de autorizacion."));
        }

        if (lastState != null && state != null && !lastState.equals(state)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.TEXT_HTML)
                    .body(renderCallbackPage(false, "Estado invalido."));
        }

        EntraIdConfig config;
        try {
            config = configLoader.load();
        } catch (IOException ex) {
            log.error("Entra callback: error leyendo EntraID_Conf.json", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_HTML)
                    .body(renderCallbackPage(false, "Error leyendo EntraID_Conf.json."));
        }

        String tokenUrl = resolveTokenUrl(config);
        if (tokenUrl == null) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_HTML)
                    .body(renderCallbackPage(false, "No se pudo resolver el endpoint de token."));
        }

        String redirectUri = resolveRedirectUri(config);
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", config.getClientId());
        if (config.getClientSecret() != null && !config.getClientSecret().isBlank()) {
            form.add("client_secret", config.getClientSecret());
        }
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", redirectUri);
        form.add("scope", resolveInteractiveScope(config));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(tokenUrl, entity, Map.class);
            if (response == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.TEXT_HTML)
                        .body(renderCallbackPage(false, "Respuesta vacia de Entra ID."));
            }

            Object token = response.get("access_token");
            if (!(token instanceof String tokenValue)) {
                Object err = response.get("error_description");
                String message = err != null ? err.toString() : response.toString();
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.TEXT_HTML)
                        .body(renderCallbackPage(false, "Error obteniendo token: " + message));
            }

            int expiresIn = 3600;
            Object expires = response.get("expires_in");
            if (expires instanceof Number number) {
                expiresIn = number.intValue();
            }
            Instant expiresAt = Instant.now().plusSeconds(expiresIn - 30L);
            String refreshToken = response.get("refresh_token") instanceof String r ? r : null;
            tokenStore.setToken(tokenValue, expiresAt, refreshToken, null);
            log.info("Entra callback: token almacenado (expiresIn={}s)", expiresIn);

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(renderCallbackPage(true, "Login correcto. Ya puedes volver a la app."));
        } catch (RestClientException ex) {
            log.error("Entra callback: error comunicando con Entra ID", ex);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.TEXT_HTML)
                    .body(renderCallbackPage(false, "Error comunicando con Entra ID."));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        boolean hasToken = tokenStore.getValidAccessToken().isPresent();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("loggedIn", hasToken);
        response.put("account", tokenStore.getAccountHint());
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<EntraLoginResponse> testInternal(EntraLoginRequest request) {
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
        if (config.getScope() == null || config.getScope().isBlank()
                || config.getScope().contains(".default")) {
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
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
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
                    <p>Puedes volver a la aplicacion: <a href="http://localhost:4200/buzones">Abrir Buzones</a></p>
                  </div>
                </body>
                </html>
                """;
        return String.format(template, title, statusClass, title, message);
    }
}
