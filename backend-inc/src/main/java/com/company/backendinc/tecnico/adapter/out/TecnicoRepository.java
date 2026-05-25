package com.company.backendinc.tecnico.adapter.out;

import com.company.backendinc.tecnico.Tecnico;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

@Repository
public class TecnicoRepository {
    private static final String COLUMN_EMAIL = "email";
    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public TecnicoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Tecnico> findActivos() {
        String sql = hasEmailColumn()
                ? "SELECT id, nombre, COALESCE(email, '') AS email FROM tecnico WHERE activo = true ORDER BY nombre ASC"
                : "SELECT id, nombre, '' AS email FROM tecnico WHERE activo = true ORDER BY nombre ASC";
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new Tecnico(rs.getLong("id"), rs.getString("nombre"), rs.getString(COLUMN_EMAIL)));
    }

    public Tecnico findActivoByNombre(String nombre) {
        String sql = hasEmailColumn()
                ? "SELECT id, nombre, COALESCE(email, '') AS email FROM tecnico WHERE activo = true AND nombre = ? LIMIT 1"
                : "SELECT id, nombre, '' AS email FROM tecnico WHERE activo = true AND nombre = ? LIMIT 1";
        List<Tecnico> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new Tecnico(rs.getLong("id"), rs.getString("nombre"), rs.getString("email")),
                nombre);
        return result.isEmpty() ? null : result.get(0);
    }

    public Tecnico findActivoByUserHint(String userHint) {
        if (userHint == null || userHint.isBlank()) {
            return null;
        }
        String normalized = userHint.trim().toLowerCase();
        return findActivos().stream()
                .filter(t -> t.getNombre() != null && t.getNombre().trim().toLowerCase().equals(normalized)
                        || t.getEmail() != null && (
                        t.getEmail().trim().toLowerCase().equals(normalized)
                                || t.getEmail().trim().toLowerCase().startsWith(normalized + "@")))
                .findFirst()
                .orElse(null);
    }

    public void create(String nombre, String email, String password) {
        String hash = encoder.encode(password == null ? "" : password);
        jdbcTemplate.update("INSERT INTO tecnico (nombre, email, password_hash, activo) VALUES (?, ?, ?, true)", nombre, email, hash);
    }

    private boolean hasEmailColumn() {
        return Boolean.TRUE.equals(jdbcTemplate.execute((Connection connection) -> {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet rs = metaData.getColumns(connection.getCatalog(), null, "tecnico", "email")) {
                return rs.next();
            }
        }));
    }
}
