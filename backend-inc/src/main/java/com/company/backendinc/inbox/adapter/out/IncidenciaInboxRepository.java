package com.company.backendinc.inbox.adapter.out;

import com.company.backendinc.inbox.IncidenciaInboxItem;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.List;
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
        String sql = "INSERT INTO incidencia_inbox "
                + "(message_id, mailbox, received_date_time, sender, subject, summary, tecnico_asignado, tecnico_email, categoria_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "mailbox = VALUES(mailbox), received_date_time = VALUES(received_date_time), sender = VALUES(sender), "
                + "subject = VALUES(subject), summary = VALUES(summary), tecnico_asignado = VALUES(tecnico_asignado), "
                + "tecnico_email = VALUES(tecnico_email), categoria_id = VALUES(categoria_id), assigned_at = CURRENT_TIMESTAMP";
        jdbcTemplate.update(sql, item.getMessageId(), item.getMailbox(), item.getReceivedDateTime(), item.getSender(), item.getSubject(),
                item.getSummary(), item.getTecnicoAsignado(), item.getTecnicoEmail(), item.getCategoriaId());
    }

    public List<IncidenciaInboxItem> list() {
        ensureTable();
        String sql = "SELECT i.id, i.message_id, i.mailbox, i.received_date_time, i.sender, i.subject, i.summary, i.tecnico_asignado, "
                + "i.tecnico_email, i.categoria_id, c.abreviatura AS categoria_abreviatura, "
                + "DATE_FORMAT(assigned_at, '%Y-%m-%dT%H:%i:%s') as assigned_at "
                + "FROM incidencia_inbox i LEFT JOIN categoria c ON c.id = i.categoria_id ORDER BY assigned_at DESC";
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
                rs.getString("assigned_at")));
    }

    public void updateCategoria(Long incidenciaId, Long categoriaId) {
        ensureTable();
        jdbcTemplate.update("UPDATE incidencia_inbox SET categoria_id = ? WHERE id = ?", categoriaId, incidenciaId);
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
