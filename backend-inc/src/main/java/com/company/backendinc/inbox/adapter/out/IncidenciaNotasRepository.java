package com.company.backendinc.inbox.adapter.out;

import com.company.backendinc.inbox.IncidenciaNota;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class IncidenciaNotasRepository {
    private final JdbcTemplate jdbcTemplate;

    public IncidenciaNotasRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addNota(Long incidenciaId, String tecnico, String observacion, String detalle, String accionRealizada) {
        ensureTable();
        jdbcTemplate.update(
                "INSERT INTO incidencia_nota (incidencia_id, tecnico, observacion, detalle, accion_realizada) VALUES (?, ?, ?, ?, ?)",
                incidenciaId, tecnico, observacion, detalle, accionRealizada);
    }

    public List<IncidenciaNota> listByIncidencia(Long incidenciaId) {
        ensureTable();
        return jdbcTemplate.query(
                "SELECT id, incidencia_id, tecnico, observacion, detalle, accion_realizada, "
                        + "DATE_FORMAT(created_at, '%Y-%m-%dT%H:%i:%s') as created_at "
                        + "FROM incidencia_nota WHERE incidencia_id = ? ORDER BY created_at ASC",
                (rs, rowNum) -> new IncidenciaNota(
                        rs.getLong("id"),
                        rs.getLong("incidencia_id"),
                        rs.getString("tecnico"),
                        rs.getString("observacion"),
                        rs.getString("detalle"),
                        rs.getString("accion_realizada"),
                        rs.getString("created_at")),
                incidenciaId);
    }

    private void ensureTable() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS incidencia_nota ("
                + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                + "incidencia_id BIGINT NOT NULL,"
                + "tecnico VARCHAR(150) NOT NULL,"
                + "observacion VARCHAR(500) NOT NULL,"
                + "detalle TEXT NULL,"
                + "accion_realizada VARCHAR(500) NULL,"
                + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                + ")");
    }
}
