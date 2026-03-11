package com.company.backendinc.mailbox;

public class Mailbox {
    private String id;
    private String nombre;
    private String servidorImap;
    private String usuario;

    public Mailbox() {
    }

    public Mailbox(String id, String nombre, String servidorImap, String usuario) {
        this.id = id;
        this.nombre = nombre;
        this.servidorImap = servidorImap;
        this.usuario = usuario;
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

    public String getServidorImap() {
        return servidorImap;
    }

    public void setServidorImap(String servidorImap) {
        this.servidorImap = servidorImap;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
}
