package com.company.backendinc.inbox;

public class IncidenciaNota {
    private Long id;
    private Long incidenciaId;
    private String tecnico;
    private String observacion;
    private String detalle;
    private String accionRealizada;
    private String createdAt;

    public IncidenciaNota(Long id, Long incidenciaId, String tecnico, String observacion, String detalle, String accionRealizada,
            String createdAt) {
        this.id = id;
        this.incidenciaId = incidenciaId;
        this.tecnico = tecnico;
        this.observacion = observacion;
        this.detalle = detalle;
        this.accionRealizada = accionRealizada;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getIncidenciaId() { return incidenciaId; }
    public String getTecnico() { return tecnico; }
    public String getObservacion() { return observacion; }
    public String getDetalle() { return detalle; }
    public String getAccionRealizada() { return accionRealizada; }
    public String getCreatedAt() { return createdAt; }
}
