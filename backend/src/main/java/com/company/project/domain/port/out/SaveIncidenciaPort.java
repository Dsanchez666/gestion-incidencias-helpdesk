package com.company.project.domain.port.out;

import com.company.project.domain.model.Incidencia;

import java.util.List;

public interface SaveIncidenciaPort {
    Incidencia save(Incidencia incidencia);
    List<Incidencia> findAll();
}
