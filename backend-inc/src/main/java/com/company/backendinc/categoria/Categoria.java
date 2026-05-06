package com.company.backendinc.categoria;

public class Categoria {
    private Long id;
    private String nombre;
    private String abreviatura;
    private String colorHex;

    public Categoria() {}

    public Categoria(Long id, String nombre, String abreviatura, String colorHex) {
        this.id = id;
        this.nombre = nombre;
        this.abreviatura = abreviatura;
        this.colorHex = colorHex;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getAbreviatura() { return abreviatura; }
    public String getColorHex() { return colorHex; }
}
