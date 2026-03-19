package com.company.backendinc.mailbox.connection;

import java.util.List;

public class MailboxFolderResult {
    private String id;
    private String nombre;
    private String direccionCorreo;
    private String status;
    private String error;
    private List<MailFolder> folders;

    public MailboxFolderResult() {
    }

    public MailboxFolderResult(String id, String nombre, String direccionCorreo, String status, String error,
            List<MailFolder> folders) {
        this.id = id;
        this.nombre = nombre;
        this.direccionCorreo = direccionCorreo;
        this.status = status;
        this.error = error;
        this.folders = folders;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccionCorreo() {
        return direccionCorreo;
    }

    public void setDireccionCorreo(String direccionCorreo) {
        this.direccionCorreo = direccionCorreo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<MailFolder> getFolders() {
        return folders;
    }

    public void setFolders(List<MailFolder> folders) {
        this.folders = folders;
    }
}
