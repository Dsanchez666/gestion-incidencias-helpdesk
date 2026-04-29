package com.company.backendinc.mailbox.connection;

import com.company.backendinc.mailbox.config.MailboxConfig;
import com.company.backendinc.mailbox.config.MailboxEntry;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class GraphConnectionTester {
    private static final Logger log = LoggerFactory.getLogger(GraphConnectionTester.class);
    private final RestTemplate restTemplate = new RestTemplate();

    public List<ConnectionResult> test(MailboxConfig config, String bearerToken) {
        String baseUrl = config.getGraphBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://graph.microsoft.com/v1.0";
        }
        log.info("GraphTest: baseUrl={}, mailboxes={}", baseUrl,
                config.getMailboxes() == null ? 0 : config.getMailboxes().size());

        List<ConnectionResult> results = new ArrayList<>();
        if (config.getMailboxes() == null) {
            log.warn("GraphTest: no hay buzones configurados");
            return results;
        }

        for (MailboxEntry entry : config.getMailboxes()) {
            String url = String.format("%s/users/%s/mailFolders?$top=1", baseUrl, entry.getDireccionCorreo());
            log.info("GraphTest: GET mailbox={} id={} url={}", entry.getDireccionCorreo(), entry.getId(), url);
            results.add(request(entry, url, bearerToken));
        }

        return results;
    }

    private ConnectionResult request(MailboxEntry entry, String url, String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            log.info("GraphTest: mailbox={} status={} body={}", entry.getDireccionCorreo(), response.getStatusCode(),
                    truncate(response.getBody(), 500));
            if (response.getStatusCode().is2xxSuccessful()) {
                return new ConnectionResult(entry.getId(), entry.getNombre(), entry.getDireccionCorreo(), "ok", null);
            }
            return new ConnectionResult(entry.getId(), entry.getNombre(), entry.getDireccionCorreo(), "error",
                    "Status " + response.getStatusCode());
        } catch (RestClientException ex) {
            log.error("GraphTest: mailbox={} error={}", entry.getDireccionCorreo(), describeException(ex));
            return new ConnectionResult(entry.getId(), entry.getNombre(), entry.getDireccionCorreo(), "error", ex.getMessage());
        }
    }

    private String describeException(Exception ex) {
        if (ex instanceof HttpStatusCodeException httpEx) {
            return "status=" + httpEx.getStatusCode() + ", body=" + truncate(httpEx.getResponseBodyAsString(), 1000);
        }
        return ex.getMessage();
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return "<sin cuerpo>";
        }
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max) + "...";
    }
}
