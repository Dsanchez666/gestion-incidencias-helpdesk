package com.company.backendinc.inbox.application;

import com.company.backendinc.auth.entra.application.port.out.EntraSessionStorePort;
import com.company.backendinc.inbox.InboxItem;
import com.company.backendinc.inbox.adapter.out.MailManagementRepository;
import com.company.backendinc.inbox.adapter.out.MailManagementState;
import com.company.backendinc.mailbox.application.port.out.MailboxConfigPort;
import com.company.backendinc.mailbox.config.MailboxConfig;
import com.company.backendinc.mailbox.config.MailboxEntry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class InboxManagementUseCase {
    private final EntraSessionStorePort tokenStore;
    private final MailboxConfigPort mailboxConfigPort;
    private final MailManagementRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InboxManagementUseCase(EntraSessionStorePort tokenStore,
            MailboxConfigPort mailboxConfigPort,
            MailManagementRepository repository) {
        this.tokenStore = tokenStore;
        this.mailboxConfigPort = mailboxConfigPort;
        this.repository = repository;
    }

    public List<InboxItem> listInbox(int summaryLength) throws IOException {
        String accessToken = tokenStore.getValidAccessToken()
                .orElseThrow(() -> new IllegalStateException("Login requerido"));

        MailboxConfig config = mailboxConfigPort.load();
        String graphBaseUrl = config.getGraphBaseUrl() == null || config.getGraphBaseUrl().isBlank()
                ? "https://graph.microsoft.com/v1.0"
                : config.getGraphBaseUrl();

        List<MailboxEntry> mailboxes = config.getMailboxes();
        if (mailboxes == null || mailboxes.isEmpty()) {
            return List.of();
        }

        MailboxEntry mailbox = mailboxes.get(0);
        String url = UriComponentsBuilder.fromHttpUrl(graphBaseUrl)
                .path("/users/{id}/mailFolders/inbox/messages")
                .queryParam("$top", 100)
                .queryParam("$select", "id,subject,from,receivedDateTime,bodyPreview")
                .buildAndExpand(mailbox.getDireccionCorreo())
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        List<InboxItem> items = new ArrayList<>();
        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode value = root.path("value");

        for (JsonNode node : value) {
            String messageId = node.path("id").asText("");
            String subject = node.path("subject").asText("");
            String receivedDateTime = node.path("receivedDateTime").asText("");
            String sender = node.path("from").path("emailAddress").path("address").asText("");
            String bodyPreview = node.path("bodyPreview").asText("");
            String summary = bodyPreview.length() > summaryLength ? bodyPreview.substring(0, summaryLength) : bodyPreview;

            items.add(new InboxItem(messageId, mailbox.getDireccionCorreo(), receivedDateTime, sender, subject, summary,
                    false, false, ""));
        }

        Map<String, MailManagementState> states = repository.findByMessageIds(items.stream().map(InboxItem::getMessageId).toList());
        for (InboxItem item : items) {
            MailManagementState state = states.get(item.getMessageId());
            if (state != null) {
                item.setIncidenciaGenerada(state.isIncidenciaGenerada());
                item.setAsignada(state.isAsignada());
                item.setTecnicoAsignado(state.getTecnicoAsignado() == null ? "" : state.getTecnicoAsignado());
            }
        }

        return items;
    }

    public void updateIncidencia(String messageId, boolean incidenciaGenerada) {
        repository.upsertIncidencia(messageId, incidenciaGenerada);
    }

    public void updateAsignacion(String messageId, boolean asignada, String tecnicoAsignado) {
        repository.upsertAsignacion(messageId, asignada, tecnicoAsignado);
    }
}
