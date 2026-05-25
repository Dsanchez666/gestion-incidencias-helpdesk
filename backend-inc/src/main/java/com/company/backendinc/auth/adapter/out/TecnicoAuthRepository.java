package com.company.backendinc.auth.adapter.out;

import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TecnicoAuthRepository {
    private final JdbcTemplate jdbcTemplate;

    public TecnicoAuthRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public AuthUser findByUserHint(String userHint) {
        if (userHint == null || userHint.isBlank()) return null;
        String normalized = userHint.trim().toLowerCase();
        return jdbcTemplate.query("""
                SELECT id, nombre, email, password_hash
                FROM tecnico
                WHERE activo = true
                  AND (LOWER(nombre) = ? OR LOWER(email) = ? OR LOWER(email) LIKE CONCAT(?, '@%'))
                LIMIT 1
                """, ps -> {
            ps.setString(1, normalized);
            ps.setString(2, normalized);
            ps.setString(3, normalized);
        }, rs -> rs.next()
                ? new AuthUser(rs.getLong("id"), rs.getString("nombre"), rs.getString("email"), rs.getString("password_hash"))
                : null);
    }

    public void updatePasswordHash(long tecnicoId, String hash) {
        jdbcTemplate.update("UPDATE tecnico SET password_hash = ? WHERE id = ?", hash, tecnicoId);
    }

    public String createResetToken(long tecnicoId, Instant expiresAt) {
        String token = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "INSERT INTO tecnico_password_reset_token (tecnico_id, token, expires_at, used) VALUES (?, ?, ?, false)",
                tecnicoId, token, java.sql.Timestamp.from(expiresAt));
        return token;
    }

    public Long consumeResetToken(String token) {
        Long tecnicoId = jdbcTemplate.query("""
                SELECT tecnico_id
                FROM tecnico_password_reset_token
                WHERE token = ? AND used = false AND expires_at >= CURRENT_TIMESTAMP
                LIMIT 1
                """, ps -> ps.setString(1, token), rs -> rs.next() ? rs.getLong("tecnico_id") : null);
        if (tecnicoId != null) {
            jdbcTemplate.update("UPDATE tecnico_password_reset_token SET used = true WHERE token = ?", token);
        }
        return tecnicoId;
    }

    public record AuthUser(long id, String nombre, String email, String passwordHash) {}
}

