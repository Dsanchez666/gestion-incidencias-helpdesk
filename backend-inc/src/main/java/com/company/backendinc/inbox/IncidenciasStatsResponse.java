package com.company.backendinc.inbox;

import java.util.List;

public class IncidenciasStatsResponse {
    private String currentMonth;
    private String previousMonth;
    private List<CategoryStatsItem> categorias;
    private List<TechnicianStatsItem> tecnicos;
    private TotalsStatsItem totalizador;

    public IncidenciasStatsResponse(String currentMonth, String previousMonth, List<CategoryStatsItem> categorias,
            List<TechnicianStatsItem> tecnicos, TotalsStatsItem totalizador) {
        this.currentMonth = currentMonth;
        this.previousMonth = previousMonth;
        this.categorias = categorias;
        this.tecnicos = tecnicos;
        this.totalizador = totalizador;
    }

    public String getCurrentMonth() { return currentMonth; }
    public String getPreviousMonth() { return previousMonth; }
    public List<CategoryStatsItem> getCategorias() { return categorias; }
    public List<TechnicianStatsItem> getTecnicos() { return tecnicos; }
    public TotalsStatsItem getTotalizador() { return totalizador; }

    public record CategoryStatsItem(
            String categoriaAbreviatura,
            String categoriaNombre,
            long actualTotal,
            long actualResueltas,
            long actualSinResolver,
            long actualRechazadas,
            long anteriorTotal,
            long anteriorResueltas,
            long anteriorSinResolver,
            long anteriorRechazadas) {}

    public record TechnicianStatsItem(
            String tecnicoNombre,
            long actualAsignadas,
            long actualResueltas,
            long actualRechazadas,
            long anteriorAsignadas,
            long anteriorResueltas,
            long anteriorRechazadas) {}

    public record TotalsStatsItem(
            long actualTotal,
            long actualResueltas,
            long actualSinResolver,
            long actualRechazadas,
            long anteriorTotal,
            long anteriorResueltas,
            long anteriorSinResolver,
            long anteriorRechazadas) {}
}
