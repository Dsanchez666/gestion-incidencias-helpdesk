package com.company.backendinc.inbox;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AsignarIncidenciaRequest {
    @NotBlank
    @Size(max = 150)
    private String tecnicoNombre;
    @NotBlank
    @Pattern(regexp = "URGENTE|ALTA|NORMAL|BAJA")
    private String prioridad;

    public String getTecnicoNombre() {
        return tecnicoNombre;
    }

    public void setTecnicoNombre(String tecnicoNombre) {
        this.tecnicoNombre = tecnicoNombre;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }
}
