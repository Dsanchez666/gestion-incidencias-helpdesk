package com.company.backendinc.inbox.application;

import com.company.backendinc.auth.entra.application.port.out.EntraSessionStorePort;
import com.company.backendinc.categoria.Categoria;
import com.company.backendinc.categoria.adapter.out.CategoriaRepository;
import com.company.backendinc.inbox.IncidenciasStatsResponse;
import com.company.backendinc.inbox.InboxItem;
import com.company.backendinc.inbox.InboxContext;
import com.company.backendinc.inbox.IncidenciaInboxItem;
import com.company.backendinc.inbox.IncidenciaNota;
import com.company.backendinc.inbox.adapter.out.IncidenciaNotasRepository;
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
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final IncidenciaNotasRepository incidenciaNotasRepository;
    private final TecnicoNotificationGateway notificationGateway;
    private final CategoriaRepository categoriaRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InboxManagementUseCase(EntraSessionStorePort tokenStore,
            MailboxConfigPort mailboxConfigPort,
            MailManagementRepository repository,
            TecnicoRepository tecnicoRepository,
            IncidenciaInboxRepository incidenciaInboxRepository,
            IncidenciaNotasRepository incidenciaNotasRepository,
            TecnicoNotificationGateway notificationGateway,
            CategoriaRepository categoriaRepository) {
        this.tokenStore = tokenStore;
        this.mailboxConfigPort = mailboxConfigPort;
        this.repository = repository;
        this.tecnicoRepository = tecnicoRepository;
        this.incidenciaInboxRepository = incidenciaInboxRepository;
        this.incidenciaNotasRepository = incidenciaNotasRepository;
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

    public void assignIncidencia(String messageId, String tecnicoNombre, String prioridad, int summaryLength) throws IOException {
        Tecnico tecnico = tecnicoRepository.findActivoByNombre(tecnicoNombre);
        if (tecnico == null) {
            throw new IllegalArgumentException("Tecnico no encontrado o inactivo");
        }

        String prioridadNormalizada = normalizePrioridad(prioridad);
        InboxItem target = getInboxItemById(messageId, summaryLength);

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
                null,
                prioridadNormalizada,
                false,
                false,
                false,
                false,
                null);
        incidenciaInboxRepository.upsert(incidencia);
        byte[] originalEml = fetchOriginalMessageEml(target.getMailbox(), target.getMessageId());
        notificationGateway.notifyAssignment(tecnico.getEmail(), tecnico.getNombre(), target.getSubject(), target.getSender(),
                target.getReceivedDateTime(), target.getSummary(), target.getMailbox(), originalEml);
    }

    public void assignIncidencias(List<String> messageIds, String tecnicoNombre, String prioridad, int summaryLength) throws IOException {
        if (messageIds == null || messageIds.isEmpty()) {
            throw new IllegalArgumentException("No hay correos seleccionados");
        }
        Tecnico tecnico = tecnicoRepository.findActivoByNombre(tecnicoNombre);
        if (tecnico == null) {
            throw new IllegalArgumentException("Tecnico no encontrado o inactivo");
        }
        String prioridadNormalizada = normalizePrioridad(prioridad);
        List<InboxItem> inbox = listInbox(summaryLength);
        Map<String, InboxItem> byId = new HashMap<>();
        for (InboxItem it : inbox) byId.put(it.getMessageId(), it);

        for (String messageId : messageIds) {
            InboxItem target = byId.get(messageId);
            if (target == null) continue;

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
                    null,
                    prioridadNormalizada,
                    false,
                    false,
                    false,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);
            incidenciaInboxRepository.upsert(incidencia);
            IncidenciaInboxItem creada = incidenciaInboxRepository.list().stream()
                    .filter(i -> messageId.equals(i.getMessageId()))
                    .findFirst().orElse(null);
            if (creada != null) {
                incidenciaHistoricoRepository.addEvento(creada.getId(), currentActor(),
                        "Creada incidencia y asignada a " + tecnico.getNombre() + " con prioridad " + prioridadNormalizada);
            }
            byte[] originalEml = fetchOriginalMessageEml(target.getMailbox(), target.getMessageId());
            notificationGateway.notifyAssignment(tecnico.getEmail(), tecnico.getNombre(), target.getSubject(), target.getSender(),
                    target.getReceivedDateTime(), target.getSummary(), target.getMailbox(), originalEml);
        }
    }

    private String normalizePrioridad(String prioridad) {
        if (prioridad == null || prioridad.isBlank()) return "NORMAL";
        String p = prioridad.trim().toUpperCase();
        if (!List.of("URGENTE", "ALTA", "NORMAL", "BAJA").contains(p)) {
            throw new IllegalArgumentException("Prioridad no valida");
        }
        return p;
    }

    public void updateCategoria(Long incidenciaId, Long categoriaId) {
        Categoria categoria = categoriaRepository.findById(categoriaId);
        if (categoria == null) {
            throw new IllegalArgumentException("Categoria no valida");
        }
        incidenciaInboxRepository.updateCategoria(incidenciaId, categoriaId);
        incidenciaHistoricoRepository.addEvento(incidenciaId, currentActor(), "Cambio de categoría a id=" + categoriaId);
    }

    public void updateTecnicoIncidencia(Long incidenciaId, String tecnicoNombre) {
        Tecnico tecnico = tecnicoRepository.findActivoByNombre(tecnicoNombre);
        if (tecnico == null) {
            throw new IllegalArgumentException("Tecnico no encontrado o inactivo");
        }
        incidenciaInboxRepository.updateTecnico(incidenciaId, tecnico.getNombre(), tecnico.getEmail());
        incidenciaHistoricoRepository.addEvento(incidenciaId, currentActor(), "Reasignada a técnico " + tecnico.getNombre());
    }

    public void redirectIncidencia(Long incidenciaId, String tecnicoNombre) {
        ensureNotConsulta();
        Tecnico tecnico = tecnicoRepository.findActivoByNombre(tecnicoNombre);
        if (tecnico == null) {
            throw new IllegalArgumentException("Tecnico no encontrado o inactivo");
        }
        IncidenciaInboxItem inc = incidenciaInboxRepository.findById(incidenciaId);
        if (inc == null) throw new IllegalArgumentException("Incidencia no encontrada");
        incidenciaInboxRepository.updateTecnico(incidenciaId, tecnico.getNombre(), tecnico.getEmail());
        incidenciaHistoricoRepository.addEvento(incidenciaId, currentActor(), "Redirigida incidencia a " + tecnico.getNombre());
        notificationGateway.notifyAssignment(tecnico.getEmail(), tecnico.getNombre(), inc.getSubject(), inc.getSender(),
                inc.getReceivedDateTime(), inc.getSummary(), inc.getMailbox(), null);
    }

    public void updateResuelta(Long incidenciaId, boolean resuelta) {
        incidenciaInboxRepository.updateResuelta(incidenciaId, resuelta);
        incidenciaHistoricoRepository.addEvento(incidenciaId, currentActor(), resuelta ? "Incidencia marcada como RESUELTA" : "Incidencia reabierta");
    }

    public void updatePrioridad(Long incidenciaId, String prioridad) {
        String p = normalizePrioridad(prioridad);
        incidenciaInboxRepository.updatePrioridad(incidenciaId, p);
        incidenciaHistoricoRepository.addEvento(incidenciaId, currentActor(), "Cambio de prioridad a " + p);
    }

    public void resolveIncidencia(Long incidenciaId, String descripcionResolucion) {
        ensureNotConsulta();
        if (descripcionResolucion == null || descripcionResolucion.isBlank()) {
            throw new IllegalArgumentException("Descripcion de resolución obligatoria");
        }
        IncidenciaInboxItem inc = incidenciaInboxRepository.findById(incidenciaId);
        if (inc == null) throw new IllegalArgumentException("Incidencia no encontrada");
        String actor = currentActor();
        incidenciaInboxRepository.resolveWithDescription(incidenciaId, descripcionResolucion, actor);
        incidenciaHistoricoRepository.addEvento(incidenciaId, actor, "Incidencia resuelta: " + descripcionResolucion);
        String token = incidenciaTrackingRepository.createToken(incidenciaId);
        String enlace = "http://localhost:4200/seguimiento/" + token;
        notificationGateway.notifyResolutionToSender(inc.getSender(), inc.getSubject(), descripcionResolucion, enlace);
    }

    public void rejectResolution(Long incidenciaId, String motivo) {
        if (motivo == null || motivo.isBlank()) throw new IllegalArgumentException("Motivo obligatorio");
        IncidenciaInboxItem inc = incidenciaInboxRepository.findById(incidenciaId);
        if (inc == null) throw new IllegalArgumentException("Incidencia no encontrada");
        String actor = currentActor();
        String actorUser = actor.contains("@") ? actor.substring(0, actor.indexOf('@')) : actor;
        String senderUser = inc.getSender() != null && inc.getSender().contains("@")
                ? inc.getSender().substring(0, inc.getSender().indexOf('@'))
                : (inc.getSender() == null ? "" : inc.getSender());
        if (!actorUser.equalsIgnoreCase(senderUser)) {
            throw new IllegalArgumentException("Solo el remitente puede rechazar la resolución");
        }
        incidenciaInboxRepository.rejectResolution(incidenciaId, motivo, actor);
        incidenciaHistoricoRepository.addEvento(incidenciaId, actor, "Resolución rechazada: " + motivo);
    }

    public IncidenciasStatsResponse getStats() {
        YearMonth current = YearMonth.now();
        YearMonth previous = current.minusMonths(1);

        List<Map<String, Object>> currentCat = incidenciaInboxRepository.categoryStatsByAssignedMonth(current);
        List<Map<String, Object>> previousCat = incidenciaInboxRepository.categoryStatsByAssignedMonth(previous);

        Map<String, IncidenciasStatsResponse.CategoryStatsItem> merged = new HashMap<>();
        for (Map<String, Object> row : currentCat) {
            String abrv = String.valueOf(row.get("abrv"));
            String nombre = String.valueOf(row.get("nombre"));
            long total = ((Number) row.get("total")).longValue();
            long resueltas = ((Number) row.get("resueltas")).longValue();
            merged.put(abrv, new IncidenciasStatsResponse.CategoryStatsItem(abrv, nombre, total, resueltas, total - resueltas, 0, 0, 0, 0, 0));
        }
        for (Map<String, Object> row : previousCat) {
            String abrv = String.valueOf(row.get("abrv"));
            String nombre = String.valueOf(row.get("nombre"));
            long total = ((Number) row.get("total")).longValue();
            long resueltas = ((Number) row.get("resueltas")).longValue();
            IncidenciasStatsResponse.CategoryStatsItem existing = merged.get(abrv);
            if (existing == null) {
                merged.put(abrv, new IncidenciasStatsResponse.CategoryStatsItem(abrv, nombre, 0, 0, 0, 0, total, resueltas, total - resueltas, 0));
            } else {
                merged.put(abrv, new IncidenciasStatsResponse.CategoryStatsItem(
                        existing.categoriaAbreviatura(), existing.categoriaNombre(),
                        existing.actualTotal(), existing.actualResueltas(), existing.actualSinResolver(), existing.actualRechazadas(),
                        total, resueltas, total - resueltas, existing.anteriorRechazadas()));
            }
        }

        Map<String, Long> currentAssigned = toMap(incidenciaInboxRepository.technicianAssignedStats(current), "asignadas");
        Map<String, Long> previousAssigned = toMap(incidenciaInboxRepository.technicianAssignedStats(previous), "asignadas");
        Map<String, Long> currentResolved = toMap(incidenciaInboxRepository.technicianResolvedStats(current), "resueltas");
        Map<String, Long> previousResolved = toMap(incidenciaInboxRepository.technicianResolvedStats(previous), "resueltas");
        Map<String, Long> currentRejectedTech = toMap(incidenciaInboxRepository.technicianRejectedStats(current), "rechazadas");
        Map<String, Long> previousRejectedTech = toMap(incidenciaInboxRepository.technicianRejectedStats(previous), "rechazadas");
        Map<String, Long> currentRejectedCat = toMapByKey(incidenciaInboxRepository.categoryRejectedStatsByAssignedMonth(current), "abrv", "rechazadas");
        Map<String, Long> previousRejectedCat = toMapByKey(incidenciaInboxRepository.categoryRejectedStatsByAssignedMonth(previous), "abrv", "rechazadas");

        Map<String, IncidenciasStatsResponse.TechnicianStatsItem> techMerged = new HashMap<>();
        for (String tech : currentAssigned.keySet()) {
            techMerged.put(tech, new IncidenciasStatsResponse.TechnicianStatsItem(
                    tech, currentAssigned.getOrDefault(tech, 0L), currentResolved.getOrDefault(tech, 0L), currentRejectedTech.getOrDefault(tech, 0L),
                    previousAssigned.getOrDefault(tech, 0L), previousResolved.getOrDefault(tech, 0L), previousRejectedTech.getOrDefault(tech, 0L)));
        }
        for (String tech : previousAssigned.keySet()) {
            techMerged.putIfAbsent(tech, new IncidenciasStatsResponse.TechnicianStatsItem(
                    tech, currentAssigned.getOrDefault(tech, 0L), currentResolved.getOrDefault(tech, 0L), currentRejectedTech.getOrDefault(tech, 0L),
                    previousAssigned.getOrDefault(tech, 0L), previousResolved.getOrDefault(tech, 0L), previousRejectedTech.getOrDefault(tech, 0L)));
        }

        for (Map.Entry<String, IncidenciasStatsResponse.CategoryStatsItem> e : new ArrayList<>(merged.entrySet())) {
            var c = e.getValue();
            merged.put(e.getKey(), new IncidenciasStatsResponse.CategoryStatsItem(
                    c.categoriaAbreviatura(), c.categoriaNombre(),
                    c.actualTotal(), c.actualResueltas(), c.actualSinResolver(), currentRejectedCat.getOrDefault(e.getKey(), 0L),
                    c.anteriorTotal(), c.anteriorResueltas(), c.anteriorSinResolver(), previousRejectedCat.getOrDefault(e.getKey(), 0L)));
        }

        long currentTotal = merged.values().stream().mapToLong(IncidenciasStatsResponse.CategoryStatsItem::actualTotal).sum();
        long currentRes = merged.values().stream().mapToLong(IncidenciasStatsResponse.CategoryStatsItem::actualResueltas).sum();
        long currentRech = merged.values().stream().mapToLong(IncidenciasStatsResponse.CategoryStatsItem::actualRechazadas).sum();
        long prevTotal = merged.values().stream().mapToLong(IncidenciasStatsResponse.CategoryStatsItem::anteriorTotal).sum();
        long prevRes = merged.values().stream().mapToLong(IncidenciasStatsResponse.CategoryStatsItem::anteriorResueltas).sum();
        long prevRech = merged.values().stream().mapToLong(IncidenciasStatsResponse.CategoryStatsItem::anteriorRechazadas).sum();

        IncidenciasStatsResponse.TotalsStatsItem totals = new IncidenciasStatsResponse.TotalsStatsItem(
                currentTotal, currentRes, currentTotal - currentRes, currentRech,
                prevTotal, prevRes, prevTotal - prevRes, prevRech);

        return new IncidenciasStatsResponse(
                current.toString(),
                previous.toString(),
                new ArrayList<>(merged.values()),
                new ArrayList<>(techMerged.values()),
                totals);
    }

    private Map<String, Long> toMap(List<Map<String, Object>> rows, String key) {
        Map<String, Long> out = new HashMap<>();
        for (Map<String, Object> row : rows) {
            out.put(String.valueOf(row.get("tecnico")), ((Number) row.get(key)).longValue());
        }
        return out;
    }

    public List<IncidenciaInboxItem> listIncidencias() {
        return incidenciaInboxRepository.list();
    }

    public List<IncidenciaNota> listNotas(Long incidenciaId) {
        return incidenciaNotasRepository.listByIncidencia(incidenciaId);
    }

    public void addNota(Long incidenciaId, String tecnico, String observacion, String detalle, String accionRealizada) {
        incidenciaNotasRepository.addNota(incidenciaId, tecnico, observacion, detalle, accionRealizada);
        incidenciaInboxRepository.markEnProgreso(incidenciaId, true);
    }

    private byte[] fetchOriginalMessageEml(String mailbox, String messageId) {
        try {
            String accessToken = tokenStore.getValidAccessToken().orElse(null);
            if (accessToken == null) {
                return null;
            }
            MailboxConfig config = mailboxConfigPort.load();
            String graphBaseUrl = config.getGraphBaseUrl() == null || config.getGraphBaseUrl().isBlank()
                    ? "https://graph.microsoft.com/v1.0"
                    : config.getGraphBaseUrl();
            String url = UriComponentsBuilder.fromHttpUrl(graphBaseUrl)
                    .path("/users/{id}/messages/{messageId}/$value")
                    .buildAndExpand(mailbox, messageId)
                    .toUriString();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
            return response.getBody();
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean canReadMailbox() {
        try {
            listInbox(1);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private InboxItem getInboxItemById(String messageId, int summaryLength) throws IOException {
        String accessToken = tokenStore.getValidAccessToken()
                .orElseThrow(() -> new IllegalStateException("Login requerido"));

        MailboxConfig config = mailboxConfigPort.load();
        String graphBaseUrl = config.getGraphBaseUrl() == null || config.getGraphBaseUrl().isBlank()
                ? "https://graph.microsoft.com/v1.0"
                : config.getGraphBaseUrl();
        List<MailboxEntry> mailboxes = config.getMailboxes();
        if (mailboxes == null || mailboxes.isEmpty()) {
            throw new IllegalArgumentException("No hay buzones configurados");
        }
        MailboxEntry mailbox = mailboxes.get(0);

        String url = UriComponentsBuilder.fromHttpUrl(graphBaseUrl)
                .path("/users/{id}/messages/{messageId}")
                .queryParam("$select", "id,subject,from,receivedDateTime,bodyPreview")
                .buildAndExpand(mailbox.getDireccionCorreo(), messageId)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        JsonNode node = objectMapper.readTree(response.getBody());
        String bodyPreview = node.path("bodyPreview").asText("");
        String summary = bodyPreview.length() > summaryLength ? bodyPreview.substring(0, summaryLength) : bodyPreview;

        return new InboxItem(
                node.path("id").asText(""),
                mailbox.getDireccionCorreo(),
                node.path("receivedDateTime").asText(""),
                node.path("from").path("emailAddress").path("address").asText(""),
                node.path("subject").asText(""),
                summary,
                false,
                false,
                "");
    }
}
