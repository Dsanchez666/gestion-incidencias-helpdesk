package com.company.backendinc.prioridad;

public class Prioridad {
    private Long id;
    private String nombre;
    private String colorHex;

    public Prioridad() {}

    public Prioridad(Long id, String nombre, String colorHex) {
        this.id = id;
        this.nombre = nombre;
        this.colorHex = colorHex;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getColorHex() { return colorHex; }
}
