package com.company.project.domain.port.in;

import com.company.project.domain.model.Incidencia;

import java.util.List;

public interface ListIncidenciasUseCase {
    List<Incidencia> execute();
}
