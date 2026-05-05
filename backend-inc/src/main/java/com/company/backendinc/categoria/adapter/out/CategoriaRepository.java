package com.company.backendinc.categoria.adapter.out;

import com.company.backendinc.categoria.Categoria;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CategoriaRepository {
    private final JdbcTemplate jdbcTemplate;

    public CategoriaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Categoria> list() {
        ensureTable();
        return jdbcTemplate.query(
                "SELECT id, nombre, abreviatura FROM categoria ORDER BY nombre ASC",
                (rs, rowNum) -> new Categoria(rs.getLong("id"), rs.getString("nombre"), rs.getString("abreviatura")));
    }

    public Categoria findById(Long id) {
        ensureTable();
        List<Categoria> rows = jdbcTemplate.query(
                "SELECT id, nombre, abreviatura FROM categoria WHERE id = ?",
                (rs, rowNum) -> new Categoria(rs.getLong("id"), rs.getString("nombre"), rs.getString("abreviatura")),
                id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private void ensureTable() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS categoria ("
                + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                + "nombre VARCHAR(150) NOT NULL,"
                + "abreviatura VARCHAR(20) NOT NULL,"
                + "UNIQUE KEY uq_categoria_nombre (nombre),"
                + "UNIQUE KEY uq_categoria_abreviatura (abreviatura)"
                + ")");
    }
}
