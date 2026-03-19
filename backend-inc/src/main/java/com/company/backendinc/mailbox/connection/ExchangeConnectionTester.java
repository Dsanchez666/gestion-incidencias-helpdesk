package com.company.backendinc.mailbox.connection;

import com.company.backendinc.mailbox.config.MailboxConfig;
import com.company.backendinc.mailbox.config.MailboxEntry;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class ExchangeConnectionTester {
    private static final String SOAP_TEMPLATE =
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
            "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
            "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
            "xmlns:t=\"http://schemas.microsoft.com/exchange/services/2006/types\">" +
            "<soap:Header>" +
            "<t:RequestServerVersion Version=\"Exchange2016\" />" +
            "</soap:Header>" +
            "<soap:Body>" +
            "<FindFolder xmlns=\"http://schemas.microsoft.com/exchange/services/2006/messages\" " +
            "Traversal=\"Shallow\">" +
            "<FolderShape><t:BaseShape>IdOnly</t:BaseShape></FolderShape>" +
            "<ParentFolderIds><t:DistinguishedFolderId Id=\"inbox\" /></ParentFolderIds>" +
            "</FindFolder>" +
            "</soap:Body>" +
            "</soap:Envelope>";

    private final RestTemplate restTemplate = new RestTemplate();

    public List<ConnectionResult> test(MailboxConfig config, String bearerToken) {
        String ewsUrl = config.getExchangeEwsUrl();
        List<ConnectionResult> results = new ArrayList<>();
        if (ewsUrl == null || ewsUrl.isBlank()) {
            return results;
        }
        if (config.getMailboxes() == null) {
            return results;
        }

        for (MailboxEntry entry : config.getMailboxes()) {
            results.add(request(entry, ewsUrl, bearerToken));
        }

        return results;
    }

    private ConnectionResult request(MailboxEntry entry, String ewsUrl, String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);
        headers.set("Authorization", bearerToken);
        headers.set("X-AnchorMailbox", entry.getDireccionCorreo());

        HttpEntity<String> entity = new HttpEntity<>(SOAP_TEMPLATE, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(ewsUrl, entity, String.class);
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
