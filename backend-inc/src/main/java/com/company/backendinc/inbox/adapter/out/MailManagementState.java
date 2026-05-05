package com.company.backendinc.inbox.adapter.out;

public class MailManagementState {
    private final String messageId;
    private final boolean incidenciaGenerada;
    private final boolean asignada;
    private final String tecnicoAsignado;

    public MailManagementState(String messageId, boolean incidenciaGenerada, boolean asignada, String tecnicoAsignado) {
        this.messageId = messageId;
        this.incidenciaGenerada = incidenciaGenerada;
        this.asignada = asignada;
        this.tecnicoAsignado = tecnicoAsignado;
    }

    public String getMessageId() { return messageId; }
    public boolean isIncidenciaGenerada() { return incidenciaGenerada; }
    public boolean isAsignada() { return asignada; }
    public String getTecnicoAsignado() { return tecnicoAsignado; }
}
