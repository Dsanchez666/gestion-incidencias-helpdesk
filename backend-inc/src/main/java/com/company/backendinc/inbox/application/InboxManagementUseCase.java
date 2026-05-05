package com.company.backendinc.inbox.application;

import com.company.backendinc.auth.entra.application.port.out.EntraSessionStorePort;
import com.company.backendinc.categoria.Categoria;
import com.company.backendinc.categoria.adapter.out.CategoriaRepository;
import com.company.backendinc.inbox.InboxItem;
import com.company.backendinc.inbox.InboxContext;
import com.company.backendinc.inbox.IncidenciaInboxItem;
import com.company.backendinc.inbox.adapter.out.MailManagementRepository;
import com.company.backendinc.inbox.adapter.out.MailManagementState;
import com.company.backendinc.inbox.adapter.out.IncidenciaInboxRepository;
import com.company.backendinc.inbox.adapter.out.TecnicoNotificationGateway;
import com.company.backendinc.mailbox.application.port.out.MailboxConfigPort;
import com.company.backendinc.mailbox.config.MailboxConfig;
import com.company.backendinc.mailbox.config.MailboxEntry;
import com.company.backendinc.tecnico.Tecnico;
import com.company.backendinc.tecnico.adapter.out.TecnicoRepository;
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
    private final TecnicoRepository tecnicoRepository;
    private final IncidenciaInboxRepository incidenciaInboxRepository;
    private final TecnicoNotificationGateway notificationGateway;
    private final CategoriaRepository categoriaRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InboxManagementUseCase(EntraSessionStorePort tokenStore,
            MailboxConfigPort mailboxConfigPort,
            MailManagementRepository repository,
            TecnicoRepository tecnicoRepository,
            IncidenciaInboxRepository incidenciaInboxRepository,
            TecnicoNotificationGateway notificationGateway,
            CategoriaRepository categoriaRepository) {
        this.tokenStore = tokenStore;
        this.mailboxConfigPort = mailboxConfigPort;
        this.repository = repository;
        this.tecnicoRepository = tecnicoRepository;
        this.incidenciaInboxRepository = incidenciaInboxRepository;
        this.notificationGateway = notificationGateway;
        this.categoriaRepository = categoriaRepository;
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

    public InboxContext getContext() throws IOException {
        MailboxConfig config = mailboxConfigPort.load();
        List<MailboxEntry> mailboxes = config.getMailboxes();
        MailboxEntry mailbox = (mailboxes == null || mailboxes.isEmpty()) ? null : mailboxes.get(0);
        String account = tokenStore.getAccountHint();
        String permisos = "Mail.Read";
        String usuario = account == null || account.isBlank() ? "Usuario Entra autenticado" : account;
        String usuarioSimple = usuario.contains("@") ? usuario.substring(0, usuario.indexOf('@')) : usuario;

        Tecnico tecnico = tecnicoRepository.findActivoByUserHint(usuarioSimple);
        boolean canReadMailbox = canReadMailbox();
        String perfil;
        boolean puedeVerCorreos;

        if (tecnico != null) {
            perfil = "RESOLUTOR";
            puedeVerCorreos = false;
        } else if (canReadMailbox) {
            perfil = "ADMIN";
            puedeVerCorreos = true;
        } else {
            perfil = "CONSULTA";
            puedeVerCorreos = false;
        }

        return new InboxContext(
                "Gestion de Buzon de Incidencias",
                mailbox == null ? "No configurado" : mailbox.getNombre(),
                mailbox == null ? "-" : mailbox.getDireccionCorreo(),
                usuario,
                permisos,
                perfil,
                puedeVerCorreos);
    }

    public void assignIncidencia(String messageId, String tecnicoNombre, int summaryLength) throws IOException {
        Tecnico tecnico = tecnicoRepository.findActivoByNombre(tecnicoNombre);
        if (tecnico == null) {
            throw new IllegalArgumentException("Tecnico no encontrado o inactivo");
        }

        List<InboxItem> inbox = listInbox(summaryLength);
        InboxItem target = inbox.stream().filter(item -> messageId.equals(item.getMessageId())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Correo no encontrado en bandeja"));

        repository.upsertIncidencia(messageId, true);
        repository.upsertAsignacion(messageId, true, tecnico.getNombre());

        IncidenciaInboxItem incidencia = new IncidenciaInboxItem(
                null,
                target.getMessageId(),
                target.getMailbox(),
                target.getReceivedDateTime(),
                target.getSender(),
                target.getSubject(),
                target.getSummary(),
                tecnico.getNombre(),
                tecnico.getEmail(),
                null,
                null,
                null);
        incidenciaInboxRepository.upsert(incidencia);
        notificationGateway.notifyAssignment(tecnico.getEmail(), tecnico.getNombre(), target.getSubject(), target.getSender());
    }

    public void assignIncidencias(List<String> messageIds, String tecnicoNombre, int summaryLength) throws IOException {
        if (messageIds == null || messageIds.isEmpty()) {
            throw new IllegalArgumentException("No hay correos seleccionados");
        }
        for (String messageId : messageIds) {
            assignIncidencia(messageId, tecnicoNombre, summaryLength);
        }
    }

    public void updateCategoria(Long incidenciaId, Long categoriaId) {
        Categoria categoria = categoriaRepository.findById(categoriaId);
        if (categoria == null) {
            throw new IllegalArgumentException("Categoria no valida");
        }
        incidenciaInboxRepository.updateCategoria(incidenciaId, categoriaId);
    }

    public List<IncidenciaInboxItem> listIncidencias() {
        return incidenciaInboxRepository.list();
    }

    private boolean canReadMailbox() {
        try {
            listInbox(1);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
