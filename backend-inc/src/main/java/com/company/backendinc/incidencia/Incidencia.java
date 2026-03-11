package com.company.backendinc.incidencia;

public class Incidencia {
    private String id;
    private String asunto;
    private String descripcion;
    private String emailSolicitante;
    private String prioridad;
    private String estado;
    private String creadaEn;

    public Incidencia() {
    }

    public Incidencia(
            String id,
            String asunto,
            String descripcion,
            String emailSolicitante,
            String prioridad,
            String estado,
            String creadaEn
    ) {
        this.id = id;
        this.asunto = asunto;
        this.descripcion = descripcion;
        this.emailSolicitante = emailSolicitante;
        this.prioridad = prioridad;
        this.estado = estado;
        this.creadaEn = creadaEn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEmailSolicitante() {
        return emailSolicitante;
    }

    public void setEmailSolicitante(String emailSolicitante) {
        this.emailSolicitante = emailSolicitante;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCreadaEn() {
        return creadaEn;
    }

    public void setCreadaEn(String creadaEn) {
        this.creadaEn = creadaEn;
    }
}
