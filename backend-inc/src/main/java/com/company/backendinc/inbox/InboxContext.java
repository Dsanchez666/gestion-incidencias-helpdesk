package com.company.backendinc.inbox;

public class InboxContext {
    private String appName;
    private String mailboxNombre;
    private String mailboxCorreo;
    private String usuarioConectado;
    private String permisos;
    private String perfil;
    private boolean puedeVerCorreos;

    public InboxContext(String appName, String mailboxNombre, String mailboxCorreo, String usuarioConectado, String permisos,
            String perfil, boolean puedeVerCorreos) {
        this.appName = appName;
        this.mailboxNombre = mailboxNombre;
        this.mailboxCorreo = mailboxCorreo;
        this.usuarioConectado = usuarioConectado;
        this.permisos = permisos;
        this.perfil = perfil;
        this.puedeVerCorreos = puedeVerCorreos;
    }

    public String getAppName() {
        return appName;
    }

    public String getMailboxNombre() {
        return mailboxNombre;
    }

    public String getMailboxCorreo() {
        return mailboxCorreo;
    }

    public String getUsuarioConectado() {
        return usuarioConectado;
    }

    public String getPermisos() {
        return permisos;
    }

    public String getPerfil() {
        return perfil;
    }

    public boolean isPuedeVerCorreos() {
        return puedeVerCorreos;
    }
}
