package com.company.project.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "incidencias")
public class IncidenciaJpaEntity {
    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(nullable = false, length = 120)
    private String asunto;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Column(nullable = false, length = 200)
    private String emailSolicitante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private com.company.project.domain.model.Prioridad prioridad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private com.company.project.domain.model.Estado estado;

    @Column(nullable = false)
    private Instant creadaEn;

    protected IncidenciaJpaEntity() {
    }

    public IncidenciaJpaEntity(String id, String asunto, String descripcion, String emailSolicitante,
                               com.company.project.domain.model.Prioridad prioridad,
                               com.company.project.domain.model.Estado estado,
                               Instant creadaEn) {
        this.id = id;
        this.asunto = asunto;
        this.descripcion = descripcion;
        this.emailSolicitante = emailSolicitante;
        this.prioridad = prioridad;
        this.estado = estado;
        this.creadaEn = creadaEn;
    }

    public String getId() { return id; }
    public String getAsunto() { return asunto; }
    public String getDescripcion() { return descripcion; }
    public String getEmailSolicitante() { return emailSolicitante; }
    public com.company.project.domain.model.Prioridad getPrioridad() { return prioridad; }
    public com.company.project.domain.model.Estado getEstado() { return estado; }
    public Instant getCreadaEn() { return creadaEn; }
}
