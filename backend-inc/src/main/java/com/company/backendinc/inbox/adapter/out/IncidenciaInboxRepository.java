package com.company.backendinc.inbox.adapter.out;

import com.company.backendinc.inbox.IncidenciaInboxItem;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class IncidenciaInboxRepository {
    private final JdbcTemplate jdbcTemplate;

    public IncidenciaInboxRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void upsert(IncidenciaInboxItem item) {
        ensureTable();
        Long prioridadId = null;
        if (item.getPrioridad() != null && !item.getPrioridad().isBlank()) {
            // Resolver el ID de prioridad desde el nombre
            String sql2 = "SELECT id FROM prioridad WHERE nombre = ?";
            List<Long> ids = jdbcTemplate.queryForList(sql2, Long.class, item.getPrioridad());
            if (!ids.isEmpty()) {
                prioridadId = ids.get(0);
            }
        }
        // Si no se encuentra, usa NORMAL por defecto
        if (prioridadId == null) {
            String sql2 = "SELECT id FROM prioridad WHERE nombre = 'NORMAL'";
            List<Long> ids = jdbcTemplate.queryForList(sql2, Long.class);
            if (!ids.isEmpty()) {
                prioridadId = ids.get(0);
            }
        }
        String sql = "INSERT INTO incidencia_inbox "
                + "(message_id, mailbox, received_date_time, sender, subject, summary, tecnico_asignado, tecnico_email, categoria_id, prioridad_id, resuelta, en_progreso, resolved_at, assigned_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, false, false, NULL, CURRENT_TIMESTAMP) "
                + "ON DUPLICATE KEY UPDATE "
                + "mailbox = VALUES(mailbox), received_date_time = VALUES(received_date_time), sender = VALUES(sender), "
                + "subject = VALUES(subject), summary = VALUES(summary), tecnico_asignado = VALUES(tecnico_asignado), "
                + "tecnico_email = VALUES(tecnico_email), categoria_id = VALUES(categoria_id), prioridad_id = VALUES(prioridad_id), assigned_at = CURRENT_TIMESTAMP";
        jdbcTemplate.update(sql, item.getMessageId(), item.getMailbox(), item.getReceivedDateTime(), item.getSender(), item.getSubject(),
                item.getSummary(), item.getTecnicoAsignado(), item.getTecnicoEmail(), item.getCategoriaId(), prioridadId);
    }

    public List<IncidenciaInboxItem> list() {
        ensureTable();
        String sql = "SELECT i.id, i.message_id, i.mailbox, i.received_date_time, i.sender, i.subject, i.summary, i.tecnico_asignado, "
                + "i.tecnico_email, i.categoria_id, c.abreviatura AS categoria_abreviatura, c.color_hex AS categoria_color_hex, "
                + "COALESCE(p.nombre, 'NORMAL') AS prioridad, i.resuelta, i.en_progreso, "
                + "DATE_FORMAT(i.assigned_at, '%Y-%m-%dT%H:%i:%s') as assigned_at "
                + "FROM incidencia_inbox i LEFT JOIN categoria c ON c.id = i.categoria_id LEFT JOIN prioridad p ON p.id = i.prioridad_id ORDER BY i.assigned_at DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new IncidenciaInboxItem(
                rs.getLong("id"),
                rs.getString("message_id"),
                rs.getString("mailbox"),
                rs.getString("received_date_time"),
                rs.getString("sender"),
                rs.getString("subject"),
                rs.getString("summary"),
                rs.getString("tecnico_asignado"),
                rs.getString("tecnico_email"),
                rs.getObject("categoria_id", Long.class),
                rs.getString("categoria_abreviatura"),
                rs.getString("categoria_color_hex"),
                rs.getString("prioridad"),
                rs.getBoolean("resuelta"),
                rs.getBoolean("en_progreso"),
                rs.getString("assigned_at")));
    }

    public void updateCategoria(Long incidenciaId, Long categoriaId) {
        ensureTable();
        jdbcTemplate.update("UPDATE incidencia_inbox SET categoria_id = ? WHERE id = ?", categoriaId, incidenciaId);
    }

    public void updateTecnico(Long incidenciaId, String tecnicoNombre, String tecnicoEmail) {
        ensureTable();
        jdbcTemplate.update("UPDATE incidencia_inbox SET tecnico_asignado = ?, tecnico_email = ? WHERE id = ?",
                tecnicoNombre, tecnicoEmail, incidenciaId);
    }

    public void updateResuelta(Long incidenciaId, boolean resuelta) {
        ensureTable();
        jdbcTemplate.update(
                "UPDATE incidencia_inbox SET resuelta = ?, en_progreso = CASE WHEN ? THEN false ELSE en_progreso END, "
                        + "resolved_at = CASE WHEN ? THEN CURRENT_TIMESTAMP ELSE NULL END WHERE id = ?",
                resuelta, resuelta, resuelta, incidenciaId);
    }

    public void markEnProgreso(Long incidenciaId, boolean enProgreso) {
        ensureTable();
        jdbcTemplate.update("UPDATE incidencia_inbox SET en_progreso = ? WHERE id = ?", enProgreso, incidenciaId);
    }

    public IncidenciaInboxItem findById(Long incidenciaId) {
        ensureTable();
        String sql = "SELECT i.id, i.message_id, i.mailbox, i.received_date_time, i.sender, i.subject, i.summary, i.tecnico_asignado, "
                + "i.tecnico_email, i.categoria_id, c.abreviatura AS categoria_abreviatura, c.color_hex AS categoria_color_hex, "
                + "COALESCE(p.nombre, 'NORMAL') AS prioridad, i.resuelta, i.en_progreso, "
                + "DATE_FORMAT(i.assigned_at, '%Y-%m-%dT%H:%i:%s') as assigned_at "
                + "FROM incidencia_inbox i LEFT JOIN categoria c ON c.id = i.categoria_id LEFT JOIN prioridad p ON p.id = i.prioridad_id WHERE i.id = ? LIMIT 1";
        return jdbcTemplate.query(sql, ps -> ps.setLong(1, incidenciaId), rs -> rs.next()
                ? new IncidenciaInboxItem(
                        rs.getLong("id"),
                        rs.getString("message_id"),
                        rs.getString("mailbox"),
                        rs.getString("received_date_time"),
                        rs.getString("sender"),
                        rs.getString("subject"),
                        rs.getString("summary"),
                        rs.getString("tecnico_asignado"),
                        rs.getString("tecnico_email"),
                        rs.getObject("categoria_id", Long.class),
                        rs.getString("categoria_abreviatura"),
                        rs.getString("categoria_color_hex"),
                        rs.getString("prioridad"),
                        rs.getBoolean("resuelta"),
                        rs.getBoolean("en_progreso"),
                        rs.getString("assigned_at"))
                : null);
    }

    public IncidenciaInboxItem findByMessageId(String messageId) {
        ensureTable();
        String sql = "SELECT i.id, i.message_id, i.mailbox, i.received_date_time, i.sender, i.subject, i.summary, i.tecnico_asignado, "
                + "i.tecnico_email, i.categoria_id, c.abreviatura AS categoria_abreviatura, c.color_hex AS categoria_color_hex, "
                + "COALESCE(p.nombre, 'NORMAL') AS prioridad, i.resuelta, i.en_progreso, "
                + "DATE_FORMAT(i.assigned_at, '%Y-%m-%dT%H:%i:%s') as assigned_at "
                + "FROM incidencia_inbox i LEFT JOIN categoria c ON c.id = i.categoria_id LEFT JOIN prioridad p ON p.id = i.prioridad_id WHERE i.message_id = ? LIMIT 1";
        return jdbcTemplate.query(sql, ps -> ps.setString(1, messageId), rs -> rs.next()
                ? new IncidenciaInboxItem(
                        rs.getLong("id"),
                        rs.getString("message_id"),
                        rs.getString("mailbox"),
                        rs.getString("received_date_time"),
                        rs.getString("sender"),
                        rs.getString("subject"),
                        rs.getString("summary"),
                        rs.getString("tecnico_asignado"),
                        rs.getString("tecnico_email"),
                        rs.getObject("categoria_id", Long.class),
                        rs.getString("categoria_abreviatura"),
                        rs.getString("categoria_color_hex"),
                        rs.getString("prioridad"),
                        rs.getBoolean("resuelta"),
                        rs.getBoolean("en_progreso"),
                        rs.getString("assigned_at"))
                : null);
    }

    public void updatePrioridad(Long incidenciaId, String prioridad) {
        ensureTable();
        Long prioridadId = null;
        if (prioridad != null && !prioridad.isBlank()) {
            List<Long> ids = jdbcTemplate.queryForList("SELECT id FROM prioridad WHERE nombre = ?", Long.class, prioridad);
            if (!ids.isEmpty()) {
                prioridadId = ids.get(0);
            }
        }
        // Si no se encuentra, usa NORMAL por defecto
        if (prioridadId == null) {
            List<Long> ids = jdbcTemplate.queryForList("SELECT id FROM prioridad WHERE nombre = 'NORMAL'", Long.class);
            if (!ids.isEmpty()) {
                prioridadId = ids.get(0);
            }
        }
        jdbcTemplate.update("UPDATE incidencia_inbox SET prioridad_id = ? WHERE id = ?", prioridadId, incidenciaId);
    }

    public void resolveWithDescription(Long incidenciaId, String descripcionResolucion, String actor) {
        ensureTable();
        jdbcTemplate.update(
                "UPDATE incidencia_inbox SET resuelta = true, en_progreso = false, resolved_at = CURRENT_TIMESTAMP, "
                        + "resolucion_texto = ?, resuelta_por = ?, rechazada = false, rechazo_motivo = NULL, rechazada_por = NULL, rechazada_at = NULL "
                        + "WHERE id = ?",
                descripcionResolucion, actor, incidenciaId);
    }

    public void rejectResolution(Long incidenciaId, String motivo, String actor) {
        ensureTable();
        jdbcTemplate.update(
                "UPDATE incidencia_inbox SET rechazada = true, rechazo_motivo = ?, rechazada_por = ?, rechazada_at = CURRENT_TIMESTAMP, "
                        + "resuelta = false, resolved_at = NULL WHERE id = ?",
                motivo, actor, incidenciaId);
    }

    public List<Map<String, Object>> categoryStatsByAssignedMonth(YearMonth month) {
        ensureTable();
        String sql = """
                SELECT COALESCE(c.abreviatura, 'N/A') AS abrv,
                       COALESCE(c.nombre, 'Sin categoria') AS nombre,
                       COUNT(*) AS total,
                       SUM(CASE WHEN i.resuelta THEN 1 ELSE 0 END) AS resueltas
                FROM incidencia_inbox i
                LEFT JOIN categoria c ON c.id = i.categoria_id
                WHERE YEAR(i.assigned_at) = ? AND MONTH(i.assigned_at) = ?
                GROUP BY c.abreviatura, c.nombre
                ORDER BY nombre
                """;
        return jdbcTemplate.queryForList(sql, month.getYear(), month.getMonthValue());
    }

    public List<Map<String, Object>> technicianAssignedStats(YearMonth month) {
        ensureTable();
        String sql = """
                SELECT tecnico_asignado AS tecnico, COUNT(*) AS asignadas
                FROM incidencia_inbox
                WHERE YEAR(assigned_at) = ? AND MONTH(assigned_at) = ?
                GROUP BY tecnico_asignado
                ORDER BY tecnico_asignado
                """;
        return jdbcTemplate.queryForList(sql, month.getYear(), month.getMonthValue());
    }

    public List<Map<String, Object>> technicianResolvedStats(YearMonth month) {
        ensureTable();
        String sql = """
                SELECT tecnico_asignado AS tecnico, COUNT(*) AS resueltas
                FROM incidencia_inbox
                WHERE resuelta = true AND resolved_at IS NOT NULL
                  AND YEAR(resolved_at) = ? AND MONTH(resolved_at) = ?
                GROUP BY tecnico_asignado
                ORDER BY tecnico_asignado
                """;
        return jdbcTemplate.queryForList(sql, month.getYear(), month.getMonthValue());
    }

    public List<Map<String, Object>> categoryRejectedStatsByAssignedMonth(YearMonth month) {
        ensureTable();
        String sql = """
                SELECT COALESCE(c.abreviatura, 'N/A') AS abrv,
                       COUNT(*) AS rechazadas
                FROM incidencia_inbox i
                LEFT JOIN categoria c ON c.id = i.categoria_id
                WHERE i.rechazada = true AND i.rechazada_at IS NOT NULL
                  AND YEAR(i.rechazada_at) = ? AND MONTH(i.rechazada_at) = ?
                GROUP BY c.abreviatura
                """;
        return jdbcTemplate.queryForList(sql, month.getYear(), month.getMonthValue());
    }

    public List<Map<String, Object>> technicianRejectedStats(YearMonth month) {
        ensureTable();
        String sql = """
                SELECT tecnico_asignado AS tecnico, COUNT(*) AS rechazadas
                FROM incidencia_inbox
                WHERE rechazada = true AND rechazada_at IS NOT NULL
                  AND YEAR(rechazada_at) = ? AND MONTH(rechazada_at) = ?
                GROUP BY tecnico_asignado
                ORDER BY tecnico_asignado
                """;
        return jdbcTemplate.queryForList(sql, month.getYear(), month.getMonthValue());
    }

    public List<String> listMessageIdsWithIncidencias() {
        ensureTable();
        String sql = "SELECT DISTINCT message_id FROM incidencia_inbox WHERE message_id IS NOT NULL";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    private void ensureTable() {
        String ddl = "CREATE TABLE IF NOT EXISTS incidencia_inbox ("
                + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                + "message_id VARCHAR(255) NOT NULL,"
                + "mailbox VARCHAR(255) NOT NULL,"
                + "received_date_time VARCHAR(100) NOT NULL,"
                + "sender VARCHAR(255) NOT NULL,"
                + "subject VARCHAR(500) NOT NULL,"
                + "summary VARCHAR(1000) NOT NULL,"
                + "tecnico_asignado VARCHAR(150) NOT NULL,"
                + "tecnico_email VARCHAR(255) NOT NULL,"
                + "categoria_id BIGINT NULL,"
                + "prioridad VARCHAR(20) NOT NULL DEFAULT 'NORMAL',"
                + "resuelta BOOLEAN NOT NULL DEFAULT FALSE,"
                + "en_progreso BOOLEAN NOT NULL DEFAULT FALSE,"
                + "resolved_at TIMESTAMP NULL,"
                + "assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "UNIQUE KEY uq_incidencia_inbox_message (message_id)"
                + ")";
        jdbcTemplate.execute(ddl);
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS categoria ("
                + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                + "nombre VARCHAR(150) NOT NULL,"
                + "abreviatura VARCHAR(20) NOT NULL,"
                + "UNIQUE KEY uq_categoria_nombre (nombre),"
                + "UNIQUE KEY uq_categoria_abreviatura (abreviatura)"
                + ")");
        if (!hasColumn("incidencia_inbox", "categoria_id")) {
            jdbcTemplate.execute("ALTER TABLE incidencia_inbox ADD COLUMN categoria_id BIGINT NULL");
        }
        if (!hasColumn("incidencia_inbox", "resuelta")) {
            jdbcTemplate.execute("ALTER TABLE incidencia_inbox ADD COLUMN resuelta BOOLEAN NOT NULL DEFAULT FALSE");
        }
        if (!hasColumn("incidencia_inbox", "resolved_at")) {
            jdbcTemplate.execute("ALTER TABLE incidencia_inbox ADD COLUMN resolved_at TIMESTAMP NULL");
        }
        if (!hasColumn("incidencia_inbox", "en_progreso")) {
            jdbcTemplate.execute("ALTER TABLE incidencia_inbox ADD COLUMN en_progreso BOOLEAN NOT NULL DEFAULT FALSE");
        }
    }

    private boolean hasColumn(String tableName, String columnName) {
        return Boolean.TRUE.equals(jdbcTemplate.execute((Connection connection) -> {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet rs = metaData.getColumns(connection.getCatalog(), null, tableName, columnName)) {
                return rs.next();
            }
        }));
    }
}
