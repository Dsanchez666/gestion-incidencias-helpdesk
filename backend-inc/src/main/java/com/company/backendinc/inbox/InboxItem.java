package com.company.backendinc.inbox;

public class InboxItem {
    private String messageId;
    private String mailbox;
    private String receivedDateTime;
    private String sender;
    private String subject;
    private String summary;
    private boolean incidenciaGenerada;
    private boolean asignada;
    private String tecnicoAsignado;

    public InboxItem() {}

    public InboxItem(String messageId, String mailbox, String receivedDateTime, String sender, String subject,
            String summary, boolean incidenciaGenerada, boolean asignada, String tecnicoAsignado) {
        this.messageId = messageId;
        this.mailbox = mailbox;
        this.receivedDateTime = receivedDateTime;
        this.sender = sender;
        this.subject = subject;
        this.summary = summary;
        this.incidenciaGenerada = incidenciaGenerada;
        this.asignada = asignada;
        this.tecnicoAsignado = tecnicoAsignado;
    }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getMailbox() { return mailbox; }
    public void setMailbox(String mailbox) { this.mailbox = mailbox; }
    public String getReceivedDateTime() { return receivedDateTime; }
    public void setReceivedDateTime(String receivedDateTime) { this.receivedDateTime = receivedDateTime; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public boolean isIncidenciaGenerada() { return incidenciaGenerada; }
    public void setIncidenciaGenerada(boolean incidenciaGenerada) { this.incidenciaGenerada = incidenciaGenerada; }
    public boolean isAsignada() { return asignada; }
    public void setAsignada(boolean asignada) { this.asignada = asignada; }
    public String getTecnicoAsignado() { return tecnicoAsignado; }
    public void setTecnicoAsignado(String tecnicoAsignado) { this.tecnicoAsignado = tecnicoAsignado; }
}
