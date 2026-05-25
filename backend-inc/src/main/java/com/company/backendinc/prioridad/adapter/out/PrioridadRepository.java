package com.company.backendinc.prioridad.adapter.out;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

@Repository
public class PrioridadRepository {
    private final DataSource dataSource;

    public PrioridadRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Prioridad> list() {
        List<Prioridad> prioridades = new ArrayList<>();
        String sql = "SELECT id, nombre, color_hex FROM prioridad ORDER BY FIELD(nombre, 'URGENTE', 'ALTA', 'NORMAL', 'BAJA')";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                prioridades.add(
                    new Prioridad(
                        rs.getLong("id"),
                        rs.getString("nombre"),
                        rs.getString("color_hex")
                    )
                );
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al cargar prioridades", e) {};
        }
        return prioridades;
    }

    public Prioridad findById(Long id) {
        String sql = "SELECT id, nombre, color_hex FROM prioridad WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Prioridad(
                        rs.getLong("id"),
                        rs.getString("nombre"),
                        rs.getString("color_hex")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al cargar prioridad", e) {};
        }
        return null;
    }

    public Prioridad findByNombre(String nombre) {
        String sql = "SELECT id, nombre, color_hex FROM prioridad WHERE nombre = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Prioridad(
                        rs.getLong("id"),
                        rs.getString("nombre"),
                        rs.getString("color_hex")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al cargar prioridad", e) {};
        }
        return null;
    }
}
