package com.company.backendinc.inbox.adapter.out;

import com.company.backendinc.inbox.IncidenciaNota;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class IncidenciaHistoricoRepository {
    private final JdbcTemplate jdbcTemplate;

    public IncidenciaHistoricoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addEvento(Long incidenciaId, String actor, String descripcion) {
        ensureTable();
        jdbcTemplate.update(
                "INSERT INTO incidencia_historico (incidencia_id, actor, descripcion) VALUES (?, ?, ?)",
                incidenciaId, actor, descripcion);
    }

    public List<IncidenciaNota> listByIncidencia(Long incidenciaId) {
        ensureTable();
        return jdbcTemplate.query(
                "SELECT id, incidencia_id, actor, descripcion, DATE_FORMAT(created_at, '%Y-%m-%dT%H:%i:%s') as created_at "
                        + "FROM incidencia_historico WHERE incidencia_id = ? ORDER BY created_at ASC",
                (rs, rowNum) -> new IncidenciaNota(
                        rs.getLong("id"),
                        rs.getLong("incidencia_id"),
                        rs.getString("actor"),
                        rs.getString("descripcion"),
                        "",
                        "Cambio registrado",
                        rs.getString("created_at")),
                incidenciaId);
    }

    private void ensureTable() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS incidencia_historico ("
                + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                + "incidencia_id BIGINT NOT NULL,"
                + "actor VARCHAR(255) NOT NULL,"
                + "descripcion VARCHAR(1000) NOT NULL,"
                + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                + ")");
    }
}

