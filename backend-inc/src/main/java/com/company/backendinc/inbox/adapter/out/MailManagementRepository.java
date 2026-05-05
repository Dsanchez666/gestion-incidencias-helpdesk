package com.company.backendinc.inbox.adapter.out;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MailManagementRepository {
    private final JdbcTemplate jdbcTemplate;

    public MailManagementRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, MailManagementState> findByMessageIds(List<String> messageIds) {
        Map<String, MailManagementState> result = new HashMap<>();
        if (messageIds == null || messageIds.isEmpty()) {
            return result;
        }

        String placeholders = String.join(",", java.util.Collections.nCopies(messageIds.size(), "?"));
        String sql = "SELECT message_id, incidencia_generada, asignada, tecnico_asignado FROM mail_management WHERE message_id IN ("
                + placeholders + ")";

        jdbcTemplate.query(sql, messageIds.toArray(), rs -> {
            String messageId = rs.getString("message_id");
            result.put(messageId,
                    new MailManagementState(
                            messageId,
                            rs.getBoolean("incidencia_generada"),
                            rs.getBoolean("asignada"),
                            rs.getString("tecnico_asignado")));
        });
        return result;
    }

    public void upsertIncidencia(String messageId, boolean incidenciaGenerada) {
        String sql = "INSERT INTO mail_management (message_id, incidencia_generada, asignada, tecnico_asignado, updated_at) "
                + "VALUES (?, ?, false, '', NOW()) "
                + "ON DUPLICATE KEY UPDATE incidencia_generada = VALUES(incidencia_generada), updated_at = NOW()";
        jdbcTemplate.update(sql, messageId, incidenciaGenerada);
    }

    public void upsertAsignacion(String messageId, boolean asignada, String tecnicoAsignado) {
        String sql = "INSERT INTO mail_management (message_id, incidencia_generada, asignada, tecnico_asignado, updated_at) "
                + "VALUES (?, false, ?, ?, NOW()) "
                + "ON DUPLICATE KEY UPDATE asignada = VALUES(asignada), tecnico_asignado = VALUES(tecnico_asignado), updated_at = NOW()";
        jdbcTemplate.update(sql, messageId, asignada, tecnicoAsignado == null ? "" : tecnicoAsignado);
    }
}
