package com.company.backendinc.inbox;

public class IncidenciaInboxItem {
    private Long id;
    private String messageId;
    private String mailbox;
    private String receivedDateTime;
    private String sender;
    private String subject;
    private String summary;
    private String tecnicoAsignado;
    private String tecnicoEmail;
    private Long categoriaId;
    private String categoriaAbreviatura;
    private String assignedAt;

    public IncidenciaInboxItem() {}

    public IncidenciaInboxItem(Long id, String messageId, String mailbox, String receivedDateTime, String sender, String subject,
            String summary, String tecnicoAsignado, String tecnicoEmail, Long categoriaId, String categoriaAbreviatura,
            String assignedAt) {
        this.id = id;
        this.messageId = messageId;
        this.mailbox = mailbox;
        this.receivedDateTime = receivedDateTime;
        this.sender = sender;
        this.subject = subject;
        this.summary = summary;
        this.tecnicoAsignado = tecnicoAsignado;
        this.tecnicoEmail = tecnicoEmail;
        this.categoriaId = categoriaId;
        this.categoriaAbreviatura = categoriaAbreviatura;
        this.assignedAt = assignedAt;
    }

    public Long getId() { return id; }
    public String getMessageId() { return messageId; }
    public String getMailbox() { return mailbox; }
    public String getReceivedDateTime() { return receivedDateTime; }
    public String getSender() { return sender; }
    public String getSubject() { return subject; }
    public String getSummary() { return summary; }
    public String getTecnicoAsignado() { return tecnicoAsignado; }
    public String getTecnicoEmail() { return tecnicoEmail; }
    public Long getCategoriaId() { return categoriaId; }
    public String getCategoriaAbreviatura() { return categoriaAbreviatura; }
    public String getAssignedAt() { return assignedAt; }
}
