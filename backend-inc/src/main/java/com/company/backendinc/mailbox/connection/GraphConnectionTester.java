package com.company.backendinc.mailbox.connection;

import com.company.backendinc.mailbox.config.MailboxConfig;
import com.company.backendinc.mailbox.config.MailboxEntry;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class GraphConnectionTester {
    private final RestTemplate restTemplate = new RestTemplate();

    public List<ConnectionResult> test(MailboxConfig config, String bearerToken) {
        String baseUrl = config.getGraphBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://graph.microsoft.com/v1.0";
        }

        List<ConnectionResult> results = new ArrayList<>();
        if (config.getMailboxes() == null) {
            return results;
        }

        for (MailboxEntry entry : config.getMailboxes()) {
            String url = String.format("%s/users/%s/mailFolders?$top=1", baseUrl, entry.getDireccionCorreo());
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
            if (response.getStatusCode().is2xxSuccessful()) {
                return new ConnectionResult(entry.getId(), entry.getNombre(), entry.getDireccionCorreo(), "ok", null);
            }
            return new ConnectionResult(entry.getId(), entry.getNombre(), entry.getDireccionCorreo(), "error",
                    "Status " + response.getStatusCode());
        } catch (RestClientException ex) {
            return new ConnectionResult(entry.getId(), entry.getNombre(), entry.getDireccionCorreo(), "error", ex.getMessage());
        }
    }
}
