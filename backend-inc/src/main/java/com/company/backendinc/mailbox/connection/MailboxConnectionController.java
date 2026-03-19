package com.company.backendinc.mailbox.connection;

import com.company.backendinc.auth.entra.EntraIdConfig;
import com.company.backendinc.auth.entra.EntraIdConfigLoader;
import com.company.backendinc.auth.entra.EntraTokenStore;
import com.company.backendinc.mailbox.config.MailboxConfig;
import com.company.backendinc.mailbox.config.MailboxConfigLoader;
import com.company.backendinc.mailbox.config.MailboxEntry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/mailboxes")
public class MailboxConnectionController {
    private static final Logger log = LoggerFactory.getLogger(MailboxConnectionController.class);
    private final MailboxConfigLoader configLoader;
    private final EntraIdConfigLoader entraConfigLoader;
    private final EntraTokenStore tokenStore;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MailboxConnectionController(MailboxConfigLoader configLoader, EntraIdConfigLoader entraConfigLoader,
            EntraTokenStore tokenStore) {
        this.configLoader = configLoader;
        this.entraConfigLoader = entraConfigLoader;
        this.tokenStore = tokenStore;
    }

    @PostMapping("/graph/test")
    public ResponseEntity<List<ConnectionResult>> testGraph(@RequestHeader("Authorization") String authHeader) {
        try {
            MailboxConfig config = configLoader.load();
            GraphConnectionTester tester = new GraphConnectionTester();
            return ResponseEntity.ok(tester.test(config, authHeader));
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/exchange/test")
    public ResponseEntity<List<ConnectionResult>> testExchange(@RequestHeader("Authorization") String authHeader) {
        try {
            MailboxConfig config = configLoader.load();
            ExchangeConnectionTester tester = new ExchangeConnectionTester();
            return ResponseEntity.ok(tester.test(config, authHeader));
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/graph/trace")
    public ResponseEntity<GraphTraceResponse> traceGraph() {
        List<String> traces = new ArrayList<>();
        addTrace(traces, "trace: inicio");

        EntraIdConfig entraConfig;
        try {
            entraConfig = entraConfigLoader.load();
            addTrace(traces, "entra: config cargada");
        } catch (IOException ex) {
            addTrace(traces, "entra: error leyendo EntraID_Conf.json: " + ex.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new GraphTraceResponse(false, traces, List.of(), "Error leyendo EntraID_Conf.json"));
        }

        if (entraConfig.getClientId() == null || entraConfig.getClientId().isBlank()) {
            addTrace(traces, "entra: clientId no configurado");
            return ResponseEntity.badRequest()
                    .body(new GraphTraceResponse(false, traces, List.of(), "clientId no configurado"));
        }
        if (entraConfig.getClientSecret() == null || entraConfig.getClientSecret().isBlank()) {
            addTrace(traces, "entra: clientSecret no configurado");
            return ResponseEntity.badRequest()
                    .body(new GraphTraceResponse(false, traces, List.of(), "clientSecret no configurado"));
        }

        String tokenUrl = resolveTokenUrl(entraConfig);
        if (tokenUrl == null) {
            addTrace(traces, "entra: no se pudo resolver el token url");
            return ResponseEntity.badRequest()
                    .body(new GraphTraceResponse(false, traces, List.of(), "No se pudo resolver el token url"));
        }
        addTrace(traces, "entra: token url = " + tokenUrl);

        String scope = (entraConfig.getScope() == null || entraConfig.getScope().isBlank())
                ? "https://graph.microsoft.com/.default"
                : entraConfig.getScope();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", entraConfig.getClientId());
        form.add("client_secret", entraConfig.getClientSecret());
        form.add("grant_type", "client_credentials");
        form.add("scope", scope);

        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> tokenEntity = new HttpEntity<>(form, tokenHeaders);

        String accessToken;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> tokenResponse = restTemplate.postForObject(tokenUrl, tokenEntity, Map.class);
            if (tokenResponse == null) {
                addTrace(traces, "entra: respuesta vacia");
                return ResponseEntity.status(502)
                        .body(new GraphTraceResponse(false, traces, List.of(), "Respuesta vacia de Entra ID"));
            }
            Object tokenValue = tokenResponse.get("access_token");
            if (!(tokenValue instanceof String)) {
                Object error = tokenResponse.get("error_description");
                String errorMessage = error != null ? error.toString() : tokenResponse.toString();
                addTrace(traces, "entra: respuesta con error: " + errorMessage);
                return ResponseEntity.status(401)
                        .body(new GraphTraceResponse(false, traces, List.of(), errorMessage));
            }
            accessToken = tokenValue.toString();
            addTrace(traces, "entra: token recibido (len=" + accessToken.length() + ")");
        } catch (RestClientException ex) {
            addTrace(traces, "entra: error comunicando con Entra ID: " + ex.getMessage());
            return ResponseEntity.status(502)
                    .body(new GraphTraceResponse(false, traces, List.of(), "Error comunicando con Entra ID"));
        }

        MailboxConfig mailboxConfig;
        try {
            mailboxConfig = configLoader.load();
            addTrace(traces, "mailboxes: config cargada");
        } catch (IOException ex) {
            addTrace(traces, "mailboxes: error leyendo Mailboxes_Conf.json: " + ex.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new GraphTraceResponse(false, traces, List.of(), "Error leyendo Mailboxes_Conf.json"));
        }

        String baseUrl = mailboxConfig.getGraphBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://graph.microsoft.com/v1.0";
        }
        addTrace(traces, "graph: baseUrl = " + baseUrl);

        List<MailboxFolderResult> results = new ArrayList<>();
        if (mailboxConfig.getMailboxes() == null || mailboxConfig.getMailboxes().isEmpty()) {
            addTrace(traces, "mailboxes: lista vacia");
            return ResponseEntity.ok(new GraphTraceResponse(true, traces, results, null));
        }

        for (MailboxEntry entry : mailboxConfig.getMailboxes()) {
            results.add(fetchFoldersForMailbox(entry, baseUrl, accessToken, traces));
        }

        return ResponseEntity.ok(new GraphTraceResponse(true, traces, results, null));
    }

    @PostMapping("/graph/user/trace")
    public ResponseEntity<GraphTraceResponse> traceGraphUser() {
        List<String> traces = new ArrayList<>();
        addTrace(traces, "trace: inicio user");

        String accessToken = tokenStore.getValidAccessToken().orElse(null);
        if (accessToken == null) {
            addTrace(traces, "entra: no hay token interactivo, requiere login");
            return ResponseEntity.status(401)
                    .body(new GraphTraceResponse(false, traces, List.of(), "Login requerido"));
        }
        addTrace(traces, "entra: token interactivo presente (len=" + accessToken.length() + ")");

        MailboxConfig mailboxConfig;
        try {
            mailboxConfig = configLoader.load();
            addTrace(traces, "mailboxes: config cargada");
        } catch (IOException ex) {
            addTrace(traces, "mailboxes: error leyendo Mailboxes_Conf.json: " + ex.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new GraphTraceResponse(false, traces, List.of(), "Error leyendo Mailboxes_Conf.json"));
        }

        String baseUrl = mailboxConfig.getGraphBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://graph.microsoft.com/v1.0";
        }
        addTrace(traces, "graph: baseUrl = " + baseUrl);

        List<MailboxFolderResult> results = new ArrayList<>();
        if (mailboxConfig.getMailboxes() == null || mailboxConfig.getMailboxes().isEmpty()) {
            addTrace(traces, "mailboxes: lista vacia");
            return ResponseEntity.ok(new GraphTraceResponse(true, traces, results, null));
        }

        for (MailboxEntry entry : mailboxConfig.getMailboxes()) {
            results.add(fetchFoldersForMailbox(entry, baseUrl, accessToken, traces));
        }

        return ResponseEntity.ok(new GraphTraceResponse(true, traces, results, null));
    }

    private MailboxFolderResult fetchFoldersForMailbox(MailboxEntry entry, String baseUrl, String accessToken,
            List<String> traces) {
        String initialUrl = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/users/{id}/mailFolders")
                .queryParam("$top", 200)
                .buildAndExpand(entry.getDireccionCorreo())
                .toUriString();

        List<MailFolder> folders = new ArrayList<>();
        String nextUrl = initialUrl;
        int page = 0;

        while (nextUrl != null && !nextUrl.isBlank()) {
            page++;
            addTrace(traces, "graph: GET " + nextUrl + " (mailbox=" + entry.getDireccionCorreo() + ", page=" + page + ")");

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response;
            try {
                response = restTemplate.exchange(nextUrl, HttpMethod.GET, entity, String.class);
            } catch (RestClientException ex) {
                addTrace(traces, "graph: error de red: " + ex.getMessage());
                return new MailboxFolderResult(entry.getId(), entry.getNombre(), entry.getDireccionCorreo(), "error",
                        ex.getMessage(), folders);
            }

            if (!response.getStatusCode().is2xxSuccessful()) {
                String error = "Status " + response.getStatusCode();
                addTrace(traces, "graph: respuesta error " + error);
                return new MailboxFolderResult(entry.getId(), entry.getNombre(), entry.getDireccionCorreo(), "error",
                        error, folders);
            }

            try {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode value = root.get("value");
                if (value != null && value.isArray()) {
                    for (JsonNode node : value) {
                        String id = node.path("id").asText(null);
                        String displayName = node.path("displayName").asText(null);
                        folders.add(new MailFolder(id, displayName));
                    }
                }
                JsonNode nextLink = root.get("@odata.nextLink");
                nextUrl = nextLink != null && !nextLink.asText().isBlank() ? nextLink.asText() : null;
            } catch (IOException ex) {
                addTrace(traces, "graph: error parseando respuesta: " + ex.getMessage());
                return new MailboxFolderResult(entry.getId(), entry.getNombre(), entry.getDireccionCorreo(), "error",
                        "Error parseando respuesta Graph", folders);
            }
        }

        addTrace(traces, "graph: mailbox " + entry.getDireccionCorreo() + " carpetas=" + folders.size());
        return new MailboxFolderResult(entry.getId(), entry.getNombre(), entry.getDireccionCorreo(), "ok", null, folders);
    }

    private void addTrace(List<String> traces, String message) {
        traces.add(message);
        log.info("GraphTrace: {}", message);
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
