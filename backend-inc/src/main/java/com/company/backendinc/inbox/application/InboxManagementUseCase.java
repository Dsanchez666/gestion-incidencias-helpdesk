package com.company.backendinc.inbox.application;

import com.company.backendinc.auth.entra.application.port.out.EntraSessionStorePort;
import com.company.backendinc.categoria.Categoria;
import com.company.backendinc.categoria.adapter.out.CategoriaRepository;
import com.company.backendinc.prioridad.adapter.out.PrioridadRepository;
import com.company.backendinc.inbox.IncidenciasStatsResponse;
import com.company.backendinc.inbox.InboxItem;
import com.company.backendinc.inbox.InboxContext;
import com.company.backendinc.inbox.IncidenciaInboxItem;
import com.company.backendinc.inbox.IncidenciaNota;
import com.company.backendinc.inbox.IncidenciaSeguimientoResponse;
import com.company.backendinc.inbox.adapter.out.IncidenciaNotasRepository;
import com.company.backendinc.inbox.adapter.out.MailManagementRepository;
import com.company.backendinc.inbox.adapter.out.MailManagementState;
import com.company.backendinc.inbox.adapter.out.IncidenciaInboxRepository;
import com.company.backendinc.inbox.adapter.out.IncidenciaHistoricoRepository;
import com.company.backendinc.inbox.adapter.out.TecnicoNotificationGateway;
import com.company.backendinc.inbox.adapter.out.IncidenciaTrackingRepository;
import com.company.backendinc.mailbox.application.port.out.MailboxConfigPort;
import com.company.backendinc.mailbox.config.MailboxConfig;
import com.company.backendinc.mailbox.config.MailboxEntry;
import com.company.backendinc.tecnico.Tecnico;
import com.company.backendinc.tecnico.adapter.out.TecnicoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class InboxManagementUseCase {
    private static final Logger log = LoggerFactory.getLogger(InboxManagementUseCase.class);
    private final EntraSessionStorePort tokenStore;
    private final MailboxConfigPort mailboxConfigPort;
    private final MailManagementRepository repository;
    private final TecnicoRepository tecnicoRepository;
    private final IncidenciaInboxRepository incidenciaInboxRepository;
    private final IncidenciaNotasRepository incidenciaNotasRepository;
    private final IncidenciaHistoricoRepository incidenciaHistoricoRepository;
    private final IncidenciaTrackingRepository incidenciaTrackingRepository;
    private final TecnicoNotificationGateway notificationGateway;
    private final CategoriaRepository categoriaRepository;
    private final PrioridadRepository prioridadRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InboxManagementUseCase(EntraSessionStorePort tokenStore,
            MailboxConfigPort mailboxConfigPort,
            MailManagementRepository repository,
            TecnicoRepository tecnicoRepository,
            IncidenciaInboxRepository incidenciaInboxRepository,
            IncidenciaNotasRepository incidenciaNotasRepository,
            IncidenciaHistoricoRepository incidenciaHistoricoRepository,
            IncidenciaTrackingRepository incidenciaTrackingRepository,
            TecnicoNotificationGateway notificationGateway,
            CategoriaRepository categoriaRepository,
            PrioridadRepository prioridadRepository) {
        this.tokenStore = tokenStore;
        this.mailboxConfigPort = mailboxConfigPort;
        this.repository = repository;
        this.tecnicoRepository = tecnicoRepository;
        this.incidenciaInboxRepository = incidenciaInboxRepository;
        this.incidenciaNotasRepository = incidenciaNotasRepository;
        this.incidenciaHistoricoRepository = incidenciaHistoricoRepository;
        this.incidenciaTrackingRepository = incidenciaTrackingRepository;
        this.notificationGateway = notificationGateway;
        this.categoriaRepository = categoriaRepository;
        this.prioridadRepository = prioridadRepository;
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
        Set<String> messageIdsWithIncidencias = new HashSet<>(incidenciaInboxRepository.listMessageIdsWithIncidencias());
        
        for (InboxItem item : items) {
            // Si tiene una incidencia, marcarlo como generado
            if (messageIdsWithIncidencias.contains(item.getMessageId())) {
                item.setIncidenciaGenerada(true);
            } else {
                // Si no, usar el estado de mail_management si existe
                MailManagementState state = states.get(item.getMessageId());
                if (state != null) {
                    item.setIncidenciaGenerada(state.isIncidenciaGenerada());
                    item.setAsignada(state.isAsignada());
                    item.setTecnicoAsignado(state.getTecnicoAsignado() == null ? "" : state.getTecnicoAsignado());
                }
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
        String perfil;
        boolean puedeVerCorreos;

        if (tecnico != null) {
            perfil = "RESOLUTOR";
            puedeVerCorreos = false;
        } else {
            perfil = "ADMIN";
            puedeVerCorreos = true;
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
        Instant t0 = Instant.now();
        ensureAdmin();
        Instant t1 = Instant.now();
        Tecnico tecnico = tecnicoRepository.findActivoByNombre(tecnicoNombre);
        if (tecnico == null) {
            throw new IllegalArgumentException("Tecnico no encontrado o inactivo");
        }
        Instant t2 = Instant.now();

        String prioridadNormalizada = normalizePrioridad(prioridad);
        InboxItem target = getInboxItemById(messageId, summaryLength);
        Instant t3 = Instant.now();

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
                null);
        incidenciaInboxRepository.upsert(incidencia);
        Instant t4 = Instant.now();
        IncidenciaInboxItem creada = incidenciaInboxRepository.findByMessageId(messageId);
        if (creada != null) {
            incidenciaHistoricoRepository.addEvento(creada.getId(), currentActor(),
                    "Creada incidencia y asignada a " + tecnico.getNombre() + " con prioridad " + prioridadNormalizada);
        }
        Instant t5 = Instant.now();
        Instant t6 = Instant.now();
        notificationGateway.sendEmail(new TecnicoNotificationGateway.EmailRequest(
                tecnico.getEmail(),
                "Nueva incidencia asignada - " + target.getSubject(),
                "Hola " + tecnico.getNombre() + ",\n\n"
                        + "Se te ha asignado una incidencia nueva.\n\n"
                        + "Datos:\n"
                        + "- Buzon: " + target.getMailbox() + "\n"
                        + "- Fecha recepcion: " + target.getReceivedDateTime() + "\n"
                        + "- Remitente: " + target.getSender() + "\n"
                        + "- Asunto: " + target.getSubject() + "\n"
                        + "- Resumen: " + target.getSummary() + "\n",
                null,
                null));
        Instant t7 = Instant.now();
        log.info("assignIncidencia timing messageId={} total={}ms ensureAdmin={}ms findTecnico={}ms getInboxItem={}ms upsert={}ms historico={}ms fetchEml={}ms sendEmail={}ms",
                messageId,
                ms(t0, t7), ms(t0, t1), ms(t1, t2), ms(t2, t3), ms(t3, t4), ms(t4, t5), ms(t5, t6), ms(t6, t7));
    }

    public void assignIncidencias(List<String> messageIds, String tecnicoNombre, String prioridad, int summaryLength) throws IOException {
        Instant t0 = Instant.now();
        ensureAdmin();
        if (messageIds == null || messageIds.isEmpty()) {
            throw new IllegalArgumentException("No hay correos seleccionados");
        }
        Instant t1 = Instant.now();
        Tecnico tecnico = tecnicoRepository.findActivoByNombre(tecnicoNombre);
        if (tecnico == null) {
            throw new IllegalArgumentException("Tecnico no encontrado o inactivo");
        }
        Instant t2 = Instant.now();
        String prioridadNormalizada = normalizePrioridad(prioridad);
        List<InboxItem> inbox = listInbox(summaryLength);
        Instant t3 = Instant.now();
        Map<String, InboxItem> byId = new HashMap<>();
        for (InboxItem it : inbox) byId.put(it.getMessageId(), it);
        Instant t4 = Instant.now();

        long upsertMs = 0L;
        long historicoMs = 0L;
        long fetchEmlMs = 0L;
        long sendEmailMs = 0L;
        for (String messageId : messageIds) {
            InboxItem target = byId.get(messageId);
            if (target == null) continue;

            Instant lu0 = Instant.now();
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
                    null);
            incidenciaInboxRepository.upsert(incidencia);
            Instant lu1 = Instant.now();
            upsertMs += ms(lu0, lu1);
            IncidenciaInboxItem creada = incidenciaInboxRepository.findByMessageId(messageId);
            if (creada != null) {
                incidenciaHistoricoRepository.addEvento(creada.getId(), currentActor(),
                        "Creada incidencia y asignada a " + tecnico.getNombre() + " con prioridad " + prioridadNormalizada);
            }
            Instant lu2 = Instant.now();
            historicoMs += ms(lu1, lu2);
            Instant lu3 = Instant.now();
            notificationGateway.sendEmail(new TecnicoNotificationGateway.EmailRequest(
                    tecnico.getEmail(),
                    "Nueva incidencia asignada - " + target.getSubject(),
                    "Hola " + tecnico.getNombre() + ",\n\n"
                            + "Se te ha asignado una incidencia nueva.\n\n"
                            + "Datos:\n"
                            + "- Buzon: " + target.getMailbox() + "\n"
                            + "- Fecha recepcion: " + target.getReceivedDateTime() + "\n"
                            + "- Remitente: " + target.getSender() + "\n"
                            + "- Asunto: " + target.getSubject() + "\n"
                            + "- Resumen: " + target.getSummary() + "\n",
                    null,
                    null));
            Instant lu4 = Instant.now();
            sendEmailMs += ms(lu3, lu4);
        }
        Instant t5 = Instant.now();
        log.info("assignIncidencias timing total={}ms count={} ensureAdmin={}ms findTecnico={}ms listInbox={}ms indexInbox={}ms upsertTotal={}ms historicoTotal={}ms fetchEmlTotal={}ms sendEmailTotal={}ms",
                ms(t0, t5), messageIds.size(), ms(t0, t1), ms(t1, t2), ms(t2, t3), ms(t3, t4), upsertMs, historicoMs, fetchEmlMs, sendEmailMs);
    }

    private String normalizePrioridad(String prioridad) {
        if (prioridad == null || prioridad.isBlank()) return "NORMAL";
        String p = prioridad.trim().toUpperCase();
        if (prioridadRepository.findByNombre(p) == null) {
            throw new IllegalArgumentException("Prioridad no valida");
        }
        return p;
    }

    public void updateCategoria(Long incidenciaId, Long categoriaId) {
        ensureAdmin();
        if (categoriaId == null) {
            incidenciaInboxRepository.updateCategoria(incidenciaId, null);
            incidenciaHistoricoRepository.addEvento(incidenciaId, currentActor(), "Categoría eliminada");
            return;
        }
        Categoria categoria = categoriaRepository.findById(categoriaId);
        if (categoria == null) {
            throw new IllegalArgumentException("Categoria no valida");
        }
        incidenciaInboxRepository.updateCategoria(incidenciaId, categoriaId);
        incidenciaHistoricoRepository.addEvento(incidenciaId, currentActor(), "Cambio de categoría a " + categoria.getAbreviatura());
    }

    public void updateTecnicoIncidencia(Long incidenciaId, String tecnicoNombre) {
        ensureAdmin();
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
        notificationGateway.sendEmail(new TecnicoNotificationGateway.EmailRequest(
                tecnico.getEmail(),
                "Incidencia redirigida - " + inc.getSubject(),
                "Hola " + tecnico.getNombre() + ",\n\n"
                        + "Se te ha redirigido una incidencia.\n\n"
                        + "Datos:\n"
                        + "- Buzon: " + inc.getMailbox() + "\n"
                        + "- Fecha recepcion: " + inc.getReceivedDateTime() + "\n"
                        + "- Remitente: " + inc.getSender() + "\n"
                        + "- Asunto: " + inc.getSubject() + "\n"
                        + "- Resumen: " + inc.getSummary() + "\n",
                null,
                null));
    }

    public void updateResuelta(Long incidenciaId, boolean resuelta) {
        ensureNotConsulta();
        incidenciaInboxRepository.updateResuelta(incidenciaId, resuelta);
        incidenciaHistoricoRepository.addEvento(incidenciaId, currentActor(), resuelta ? "Incidencia marcada como RESUELTA" : "Incidencia reabierta");
    }

    public void updatePrioridad(Long incidenciaId, String prioridad) {
        ensureAdmin();
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
        notificationGateway.sendEmail(new TecnicoNotificationGateway.EmailRequest(
                inc.getSender(),
                "Incidencia resuelta - " + inc.getSubject(),
                "Tu incidencia ha sido marcada como resuelta.\n\n"
                        + "Asunto: " + inc.getSubject() + "\n"
                        + "Resolución: " + descripcionResolucion + "\n\n"
                        + "Puedes consultar el detalle en:\n"
                        + enlace + "\n",
                null,
                null));
    }

    public void rejectResolution(Long incidenciaId, String motivo) {
        if (motivo == null || motivo.isBlank()) throw new IllegalArgumentException("Motivo obligatorio");
        IncidenciaInboxItem inc = incidenciaInboxRepository.findById(incidenciaId);
        if (inc == null) throw new IllegalArgumentException("Incidencia no encontrada");
        String actor = currentActor();
        String actorUser = actor.contains("@") ? actor.substring(0, actor.indexOf('@')) : actor;
        String senderEmail = inc.getSender();
        String senderUser = senderEmail != null && senderEmail.contains("@")
                ? senderEmail.substring(0, senderEmail.indexOf('@'))
                : (senderEmail == null ? "" : senderEmail);
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

    private Map<String, Long> toMapByKey(List<Map<String, Object>> rows, String mapKey, String valueKey) {
        Map<String, Long> out = new HashMap<>();
        for (Map<String, Object> row : rows) {
            out.put(String.valueOf(row.get(mapKey)), ((Number) row.get(valueKey)).longValue());
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
        ensureNotConsulta();
        incidenciaNotasRepository.addNota(incidenciaId, tecnico, observacion, detalle, accionRealizada);
        incidenciaInboxRepository.markEnProgreso(incidenciaId, true);
        incidenciaHistoricoRepository.addEvento(incidenciaId, currentActor(), "Añadida nota: " + observacion);
    }

    public List<Object> getHistoricoIncidencia(Long incidenciaId) {
        return new ArrayList<>(incidenciaHistoricoRepository.listByIncidencia(incidenciaId));
    }

    public IncidenciaSeguimientoResponse seguimientoByToken(String token) {
        Long incidenciaId = incidenciaTrackingRepository.findIncidenciaIdByToken(token);
        if (incidenciaId == null) {
            throw new IllegalArgumentException("Token de seguimiento no válido");
        }
        IncidenciaInboxItem incidencia = incidenciaInboxRepository.findById(incidenciaId);
        if (incidencia == null) {
            throw new IllegalArgumentException("Incidencia no encontrada");
        }
        List<IncidenciaNota> historico = incidenciaHistoricoRepository.listByIncidencia(incidenciaId);
        String tiempoResolucion = "N/A";
        try {
            OffsetDateTime assigned = OffsetDateTime.parse(incidencia.getReceivedDateTime());
            Duration d = Duration.between(assigned, OffsetDateTime.now());
            long days = d.toDays();
            long hours = d.minusDays(days).toHours();
            long minutes = d.minusDays(days).minusHours(hours).toMinutes();
            tiempoResolucion = days + "d " + hours + "h " + minutes + "m";
        } catch (Exception e) {
            // If time calculation fails, leave default empty string
            tiempoResolucion = "";
        }
        return new IncidenciaSeguimientoResponse(incidencia, historico, tiempoResolucion);
    }

    private byte[] fetchOriginalMessageEml(String mailbox, String messageId) {
        try {
            String accessToken = tokenStore.getValidAccessToken().orElse(null);
            if (accessToken == null) {
                return new byte[0];
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
            return new byte[0];
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

    private String currentActor() {
        String account = tokenStore.getAccountHint();
        return (account == null || account.isBlank()) ? "sistema" : account;
    }

    private long ms(Instant start, Instant end) {
        return Duration.between(start, end).toMillis();
    }

    private void ensureNotConsulta() {
        String perfil = resolvePerfil();
        if ("CONSULTA".equals(perfil)) {
            throw new IllegalArgumentException("No autorizado para esta acción");
        }
    }

    private void ensureAdmin() {
        String perfil = resolvePerfil();
        if (!"ADMIN".equals(perfil)) {
            throw new IllegalArgumentException("No autorizado para esta acción");
        }
    }

    private String resolvePerfil() {
        String account = tokenStore.getAccountHint();
        String usuario = (account == null || account.isBlank()) ? "" : account;
        String usuarioSimple = usuario.contains("@") ? usuario.substring(0, usuario.indexOf('@')) : usuario;
        Tecnico tecnico = tecnicoRepository.findActivoByUserHint(usuarioSimple);
        return (tecnico != null) ? "RESOLUTOR" : "ADMIN";
    }
}
