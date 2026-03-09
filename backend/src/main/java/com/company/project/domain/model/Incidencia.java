package com.company.project.domain.model;

import com.company.project.domain.exception.DomainValidationException;
import com.company.project.domain.valueobject.IncidenciaId;

import java.time.Instant;
import java.util.Objects;

public final class Incidencia {
    private final IncidenciaId id;
    private final String asunto;
    private final String descripcion;
    private final String emailSolicitante;
    private final Prioridad prioridad;
    private final Estado estado;
    private final Instant creadaEn;

    private Incidencia(
            IncidenciaId id,
            String asunto,
            String descripcion,
            String emailSolicitante,
            Prioridad prioridad,
            Estado estado,
            Instant creadaEn
    ) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.asunto = validateText(asunto, 3, 120, "asunto");
        this.descripcion = validateText(descripcion, 5, 2000, "descripcion");
        this.emailSolicitante = Objects.requireNonNull(emailSolicitante, "emailSolicitante is required");
        this.prioridad = Objects.requireNonNull(prioridad, "prioridad is required");
        this.estado = Objects.requireNonNull(estado, "estado is required");
        this.creadaEn = Objects.requireNonNull(creadaEn, "creadaEn is required");
    }

    public static Incidencia nueva(
            IncidenciaId id,
            String asunto,
            String descripcion,
            String emailSolicitante,
            Prioridad prioridad
    ) {
        return new Incidencia(id, asunto, descripcion, emailSolicitante, prioridad, Estado.ABIERTA, Instant.now());
    }

    public static Incidencia rehydrate(
            IncidenciaId id,
            String asunto,
            String descripcion,
            String emailSolicitante,
            Prioridad prioridad,
            Estado estado,
            Instant creadaEn
    ) {
        return new Incidencia(id, asunto, descripcion, emailSolicitante, prioridad, estado, creadaEn);
    }

    private String validateText(String value, int min, int max, String field) {
        String trimmed = Objects.requireNonNull(value, field + " is required").trim();
        if (trimmed.length() < min || trimmed.length() > max) {
            throw new DomainValidationException(field + " length must be between " + min + " and " + max);
        }
        return trimmed;
    }

    public IncidenciaId id() { return id; }
    public String asunto() { return asunto; }
    public String descripcion() { return descripcion; }
    public String emailSolicitante() { return emailSolicitante; }
    public Prioridad prioridad() { return prioridad; }
    public Estado estado() { return estado; }
    public Instant creadaEn() { return creadaEn; }
}
