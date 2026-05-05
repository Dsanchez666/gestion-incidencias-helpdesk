package com.company.backendinc.tecnico.adapter.out;

import com.company.backendinc.tecnico.Tecnico;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TecnicoRepository {
    private final JdbcTemplate jdbcTemplate;

    public TecnicoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Tecnico> findActivos() {
        String sql = "SELECT id, nombre FROM tecnico WHERE activo = true ORDER BY nombre ASC";
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new Tecnico(rs.getLong("id"), rs.getString("nombre")));
    }
}
