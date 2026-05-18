package com.company.backendinc.inbox;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class IncidenciaNotaCreateRequest {
    @NotBlank
    @Size(max = 150)
    private String tecnico;
    @NotBlank
    @Size(max = 500)
    private String observacion;
    @Size(max = 5000)
    private String detalle;
    @Size(max = 500)
    private String accionRealizada;

    public String getTecnico() { return tecnico; }
    public void setTecnico(String tecnico) { this.tecnico = tecnico; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public String getAccionRealizada() { return accionRealizada; }
    public void setAccionRealizada(String accionRealizada) { this.accionRealizada = accionRealizada; }
}
