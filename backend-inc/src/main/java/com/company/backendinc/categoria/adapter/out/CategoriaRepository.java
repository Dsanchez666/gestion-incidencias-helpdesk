package com.company.backendinc.categoria.adapter.out;

import com.company.backendinc.categoria.Categoria;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
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
                "SELECT id, nombre, abreviatura, color_hex FROM categoria ORDER BY nombre ASC",
                (rs, rowNum) -> new Categoria(rs.getLong("id"), rs.getString("nombre"), rs.getString("abreviatura"),
                        rs.getString("color_hex")));
    }

    public Categoria findById(Long id) {
        ensureTable();
        List<Categoria> rows = jdbcTemplate.query(
                "SELECT id, nombre, abreviatura, color_hex FROM categoria WHERE id = ?",
                (rs, rowNum) -> new Categoria(rs.getLong("id"), rs.getString("nombre"), rs.getString("abreviatura"),
                        rs.getString("color_hex")),
                id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public void create(String nombre, String abreviatura, String colorHex) {
        ensureTable();
        jdbcTemplate.update("INSERT INTO categoria (nombre, abreviatura, color_hex) VALUES (?, ?, ?)",
                nombre, abreviatura, colorHex);
    }

    public void update(Long id, String nombre, String abreviatura, String colorHex) {
        ensureTable();
        jdbcTemplate.update("UPDATE categoria SET nombre = ?, abreviatura = ?, color_hex = ? WHERE id = ?",
                nombre, abreviatura, colorHex, id);
    }

    private void ensureTable() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS categoria ("
                + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                + "nombre VARCHAR(150) NOT NULL,"
                + "abreviatura VARCHAR(20) NOT NULL,"
                + "color_hex VARCHAR(20) NOT NULL DEFAULT '#f3f4f6',"
                + "UNIQUE KEY uq_categoria_nombre (nombre),"
                + "UNIQUE KEY uq_categoria_abreviatura (abreviatura)"
                + ")");
        if (!hasColumn("categoria", "color_hex")) {
            jdbcTemplate.execute("ALTER TABLE categoria ADD COLUMN color_hex VARCHAR(20) NOT NULL DEFAULT '#f3f4f6'");
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
