package com.company.backendinc.mailbox.adapter.out;

import com.company.backendinc.auth.entra.EntraIdConfig;
import com.company.backendinc.auth.entra.application.port.out.EntraConfigurationPort;
import com.company.backendinc.auth.entra.application.port.out.EntraSessionStorePort;
import com.company.backendinc.mailbox.application.port.out.GraphTracePort;
import com.company.backendinc.mailbox.application.port.out.MailboxConfigPort;
import com.company.backendinc.mailbox.config.MailboxConfig;
import com.company.backendinc.mailbox.config.MailboxEntry;
import com.company.backendinc.mailbox.connection.GraphTraceResponse;
import com.company.backendinc.mailbox.connection.MailFolder;
import com.company.backendinc.mailbox.connection.MailboxFolderResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Outbound adapter for Graph tracing against configured mailboxes.
 */
@Component
public class GraphTraceAdapter implements GraphTracePort {
    private static final Logger log = LoggerFactory.getLogger(GraphTraceAdapter.class);
    private final MailboxConfigPort mailboxConfigPort;
    private final EntraConfigurationPort entraConfigurationPort;
    private final EntraSessionStorePort tokenStore;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GraphTraceAdapter(MailboxConfigPort mailboxConfigPort,
            @Qualifier("entraIdConfigLoader") EntraConfigurationPort entraConfigurationPort,
            EntraSessionStorePort tokenStore) {
        this.mailboxConfigPort = mailboxConfigPort;
        this.entraConfigurationPort = entraConfigurationPort;
        this.tokenStore = tokenStore;
    }

    @Override
    public GraphTraceResponse traceWithAppToken() {
        List<String> traces = new ArrayList<>();
        addTrace(traces, "trace: inicio");

        EntraIdConfig entraConfig;
        try {
            entraConfig = entraConfigurationPort.load();
            addTrace(traces, "entra: config cargada");
        } catch (IOException ex) {
            addTrace(traces, "entra: error leyendo EntraID_Conf.json: " + ex.getMessage());
            return new GraphTraceResponse(false, traces, List.of(), "Error leyendo EntraID_Conf.json");
        }

        if (entraConfig.getClientId() == null || entraConfig.getClientId().isBlank()) {
            addTrace(traces, "entra: clientId no configurado");
            return new GraphTraceResponse(false, traces, List.of(), "clientId no configurado");
        }
        if (entraConfig.getClientSecret() == null || entraConfig.getClientSecret().isBlank()) {
            addTrace(traces, "entra: clientSecret no configurado");
            return new GraphTraceResponse(false, traces, List.of(), "clientSecret no configurado");
        }

        String tokenUrl = resolveTokenUrl(entraConfig);
        if (tokenUrl == null) {
            addTrace(traces, "entra: no se pudo resolver el token url");
            return new GraphTraceResponse(false, traces, List.of(), "No se pudo resolver el token url");
        }
        addTrace(traces, "entra: token url = " + tokenUrl);
        addTrace(traces, "entra: tenantId=" + safeValue(entraConfig.getTenantId()) + ", authorityUrl="
                + safeValue(entraConfig.getAuthorityUrl()) + ", clientId=" + maskClientId(entraConfig.getClientId()));

        String scope = (entraConfig.getScope() == null || entraConfig.getScope().isBlank())
                ? "https://graph.microsoft.com/.default"
                : entraConfig.getScope();
        addTrace(traces, "entra: scope=" + scope + ", grant_type=client_credentials");

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
            log.info("GraphTrace: token request POST {} clientId={} scope={}", tokenUrl,
                    maskClientId(entraConfig.getClientId()), scope);
            @SuppressWarnings("unchecked")
            Map<String, Object> tokenResponse = restTemplate.postForObject(tokenUrl, tokenEntity, Map.class);
            if (tokenResponse == null) {
                addTrace(traces, "entra: respuesta vacia");
                return new GraphTraceResponse(false, traces, List.of(), "Respuesta vacia de Entra ID");
            }
            Object tokenValue = tokenResponse.get("access_token");
            if (!(tokenValue instanceof String)) {
                Object error = tokenResponse.get("error_description");
                String errorMessage = error != null ? error.toString() : tokenResponse.toString();
                addTrace(traces, "entra: respuesta con error: " + errorMessage);
                return new GraphTraceResponse(false, traces, List.of(), errorMessage);
            }
            accessToken = tokenValue.toString();
            addTrace(traces, "entra: token recibido (len=" + accessToken.length() + ")");
        } catch (RestClientException ex) {
            addTrace(traces, "entra: error comunicando con Entra ID: " + describeHttpException(ex));
            return new GraphTraceResponse(false, traces, List.of(), "Error comunicando con Entra ID");
        }

        return traceWithToken(accessToken, traces);
    }

    @Override
    public GraphTraceResponse traceWithUserToken() {
        List<String> traces = new ArrayList<>();
        addTrace(traces, "trace: inicio user");

        String accessToken = tokenStore.getValidAccessToken().orElse(null);
        if (accessToken == null) {
            addTrace(traces, "entra: no hay token interactivo, requiere login");
            return new GraphTraceResponse(false, traces, List.of(), "Login requerido");
        }
        addTrace(traces, "entra: token interactivo presente (len=" + accessToken.length() + ")");
        return traceWithToken(accessToken, traces);
    }

    private GraphTraceResponse traceWithToken(String accessToken, List<String> traces) {
        addTrace(traces, "token: claims " + describeTokenClaims(accessToken));

        MailboxConfig mailboxConfig;
        try {
            mailboxConfig = mailboxConfigPort.load();
            addTrace(traces, "mailboxes: config cargada");
        } catch (IOException ex) {
            addTrace(traces, "mailboxes: error leyendo Mailboxes_Conf.json: " + ex.getMessage());
            return new GraphTraceResponse(false, traces, List.of(), "Error leyendo Mailboxes_Conf.json");
        }

        String baseUrl = mailboxConfig.getGraphBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://graph.microsoft.com/v1.0";
        }
        addTrace(traces, "graph: baseUrl = " + baseUrl);
        addTrace(traces, "graph: mailboxes configurados="
                + (mailboxConfig.getMailboxes() == null ? 0 : mailboxConfig.getMailboxes().size()));

        List<MailboxFolderResult> results = new ArrayList<>();
        if (mailboxConfig.getMailboxes() == null || mailboxConfig.getMailboxes().isEmpty()) {
            addTrace(traces, "mailboxes: lista vacia");
            return new GraphTraceResponse(true, traces, results, null);
        }

        for (MailboxEntry entry : mailboxConfig.getMailboxes()) {
            results.add(fetchFoldersForMailbox(entry, baseUrl, accessToken, traces));
        }
        boolean allOk = results.stream().allMatch(r -> "ok".equalsIgnoreCase(r.getStatus()));
        String globalError = allOk ? null : "Errores de acceso en uno o más buzones (revisar status/error por buzón).";
        return new GraphTraceResponse(allOk, traces, results, globalError);
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
        addTrace(traces, "graph: mailbox=" + entry.getDireccionCorreo() + " id=" + entry.getId());

        while (nextUrl != null && !nextUrl.isBlank()) {
            page++;
            addTrace(traces, "graph: GET " + nextUrl + " (mailbox=" + entry.getDireccionCorreo() + ", page=" + page + ")");

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response;
            try {
                response = restTemplate.exchange(nextUrl, HttpMethod.GET, entity, String.class);
                addTrace(traces, "graph: response status=" + response.getStatusCode() + " mailbox="
                        + entry.getDireccionCorreo() + " page=" + page);
            } catch (RestClientException ex) {
                addTrace(traces, "graph: error de red: " + describeHttpException(ex));
                if (is403(ex)) {
                    addTrace(traces, "graph: diagnostico 403 -> el token no tiene permisos efectivos sobre ese buzón "
                            + "o faltan permisos delegados compartidos/admin-consent.");
                }
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
                addTrace(traces, "graph: mailbox=" + entry.getDireccionCorreo() + " page=" + page + " folders="
                        + folders.size() + ", nextLink=" + (nextUrl != null));
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

    private String safeValue(String value) {
        return value == null || value.isBlank() ? "<vacio>" : value;
    }

    private String maskClientId(String clientId) {
        if (clientId == null || clientId.length() < 8) {
            return "<oculto>";
        }
        return clientId.substring(0, 4) + "..." + clientId.substring(clientId.length() - 4);
    }

    private String describeHttpException(Exception ex) {
        if (ex instanceof HttpStatusCodeException httpEx) {
            return "status=" + httpEx.getStatusCode() + ", body=" + truncate(httpEx.getResponseBodyAsString(), 800);
        }
        return ex.getMessage();
    }

    private String truncate(String text, int maxChars) {
        if (text == null) {
            return "<sin cuerpo>";
        }
        if (text.length() <= maxChars) {
            return text;
        }
        return text.substring(0, maxChars) + "...";
    }

    private boolean is403(Exception ex) {
        return ex instanceof HttpStatusCodeException httpEx && httpEx.getStatusCode().value() == 403;
    }

    private String describeTokenClaims(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return "sin token";
        }
        try {
            String[] parts = accessToken.split("\\.");
            if (parts.length < 2) {
                return "token no-JWT";
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            JsonNode claims = objectMapper.readTree(payload);
            String aud = claims.path("aud").asText("<sin aud>");
            String tid = claims.path("tid").asText("<sin tid>");
            String upn = claims.path("upn").asText(claims.path("preferred_username").asText("<sin usuario>"));
            String scp = claims.path("scp").asText("<sin scp>");
            JsonNode rolesNode = claims.get("roles");
            String roles = rolesNode != null ? rolesNode.toString() : "<sin roles>";
            return "aud=" + aud + ", tid=" + tid + ", user=" + upn + ", scp=" + scp + ", roles=" + roles;
        } catch (Exception ex) {
            return "no se pudieron leer claims: " + ex.getMessage();
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
