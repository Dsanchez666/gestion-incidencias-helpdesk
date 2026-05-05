package com.company.backendinc.categoria;

public class Categoria {
    private Long id;
    private String nombre;
    private String abreviatura;

    public Categoria() {}

    public Categoria(Long id, String nombre, String abreviatura) {
        this.id = id;
        this.nombre = nombre;
        this.abreviatura = abreviatura;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getAbreviatura() { return abreviatura; }
}
