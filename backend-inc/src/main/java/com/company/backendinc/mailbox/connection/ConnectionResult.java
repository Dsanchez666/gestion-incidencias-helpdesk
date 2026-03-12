package com.company.backendinc.mailbox.connection;

public class ConnectionResult {
    private String id;
    private String nombre;
    private String direccionCorreo;
    private String status;
    private String error;

    public ConnectionResult() {
    }

    public ConnectionResult(String id, String nombre, String direccionCorreo, String status, String error) {
        this.id = id;
        this.nombre = nombre;
        this.direccionCorreo = direccionCorreo;
        this.status = status;
        this.error = error;
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
}
