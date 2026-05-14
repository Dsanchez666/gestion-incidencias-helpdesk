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
    private String categoriaColorHex;
    private String prioridad;
    private boolean resuelta;
    private boolean rechazada;
    private boolean enProgreso;
    private String resolucionTexto;
    private String resueltaPor;
    private String rechazoMotivo;
    private String rechazadaPor;
    private String rechazadaAt;
    private String assignedAt;

    public IncidenciaInboxItem() {}

    public IncidenciaInboxItem(Long id, String messageId, String mailbox, String receivedDateTime, String sender, String subject,
            String summary, String tecnicoAsignado, String tecnicoEmail, Long categoriaId, String categoriaAbreviatura, String categoriaColorHex,
            String prioridad, boolean resuelta, boolean rechazada, boolean enProgreso, String resolucionTexto, String resueltaPor,
            String rechazoMotivo, String rechazadaPor, String rechazadaAt, String assignedAt) {
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
        this.categoriaColorHex = categoriaColorHex;
        this.prioridad = prioridad;
        this.resuelta = resuelta;
        this.rechazada = rechazada;
        this.enProgreso = enProgreso;
        this.resolucionTexto = resolucionTexto;
        this.resueltaPor = resueltaPor;
        this.rechazoMotivo = rechazoMotivo;
        this.rechazadaPor = rechazadaPor;
        this.rechazadaAt = rechazadaAt;
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
    public String getCategoriaColorHex() { return categoriaColorHex; }
    public String getPrioridad() { return prioridad; }
    public boolean isResuelta() { return resuelta; }
    public boolean isRechazada() { return rechazada; }
    public boolean isEnProgreso() { return enProgreso; }
    public String getResolucionTexto() { return resolucionTexto; }
    public String getResueltaPor() { return resueltaPor; }
    public String getRechazoMotivo() { return rechazoMotivo; }
    public String getRechazadaPor() { return rechazadaPor; }
    public String getRechazadaAt() { return rechazadaAt; }
    public String getAssignedAt() { return assignedAt; }
}
