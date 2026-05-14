package com.company.backendinc.inbox;

import java.util.List;

public class IncidenciaSeguimientoResponse {
    private IncidenciaInboxItem incidencia;
    private List<IncidenciaNota> historico;
    private String tiempoResolucion;

    public IncidenciaSeguimientoResponse(IncidenciaInboxItem incidencia, List<IncidenciaNota> historico, String tiempoResolucion) {
        this.incidencia = incidencia;
        this.historico = historico;
        this.tiempoResolucion = tiempoResolucion;
    }

    public IncidenciaInboxItem getIncidencia() { return incidencia; }
    public List<IncidenciaNota> getHistorico() { return historico; }
    public String getTiempoResolucion() { return tiempoResolucion; }
}
