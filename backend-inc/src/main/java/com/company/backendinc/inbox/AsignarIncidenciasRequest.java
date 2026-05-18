package com.company.backendinc.inbox;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AsignarIncidenciasRequest {
    @NotEmpty
    @Size(max = 200)
    private List<String> messageIds;
    @NotBlank
    @Size(max = 150)
    private String tecnicoNombre;
    @NotBlank
    @Pattern(regexp = "URGENTE|ALTA|NORMAL|BAJA")
    private String prioridad;

    public List<String> getMessageIds() {
        return messageIds;
    }

    public void setMessageIds(List<String> messageIds) {
        this.messageIds = messageIds;
    }

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
