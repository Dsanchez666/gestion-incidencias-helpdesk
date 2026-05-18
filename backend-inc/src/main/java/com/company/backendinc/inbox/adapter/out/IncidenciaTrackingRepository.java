package com.company.backendinc.inbox.adapter.out;

import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class IncidenciaTrackingRepository {
    private final JdbcTemplate jdbcTemplate;

    public IncidenciaTrackingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String createToken(Long incidenciaId) {
        ensureTable();
        String token = UUID.randomUUID().toString();
        jdbcTemplate.update("INSERT INTO incidencia_tracking_token (incidencia_id, token, activo) VALUES (?, ?, true)", incidenciaId, token);
        return token;
    }

    public Long findIncidenciaIdByToken(String token) {
        ensureTable();
        return jdbcTemplate.query(
                "SELECT incidencia_id FROM incidencia_tracking_token WHERE token = ? AND activo = true LIMIT 1",
                ps -> ps.setString(1, token),
                rs -> rs.next() ? rs.getLong("incidencia_id") : null);
    }

    private void ensureTable() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS incidencia_tracking_token ("
                + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                + "incidencia_id BIGINT NOT NULL,"
                + "token VARCHAR(255) NOT NULL,"
                + "activo BOOLEAN NOT NULL DEFAULT TRUE,"
                + "expires_at TIMESTAMP NULL,"
                + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "UNIQUE KEY uq_incidencia_tracking_token (token)"
                + ")");
    }
}
