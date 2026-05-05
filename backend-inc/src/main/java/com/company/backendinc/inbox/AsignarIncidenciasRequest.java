package com.company.backendinc.inbox;

import java.util.List;

public class AsignarIncidenciasRequest {
    private List<String> messageIds;
    private String tecnicoNombre;

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
}
