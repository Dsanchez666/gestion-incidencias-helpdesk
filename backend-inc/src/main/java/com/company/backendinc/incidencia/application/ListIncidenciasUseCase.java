package com.company.backendinc.incidencia.application;

import com.company.backendinc.incidencia.Incidencia;
import com.company.backendinc.incidencia.application.port.out.IncidenciaRepositoryPort;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Application use case for listing incidencias.
 */
@Service
public class ListIncidenciasUseCase {
    private final IncidenciaRepositoryPort incidenciaRepositoryPort;

    public ListIncidenciasUseCase(IncidenciaRepositoryPort incidenciaRepositoryPort) {
        this.incidenciaRepositoryPort = incidenciaRepositoryPort;
    }

    public List<Incidencia> execute() {
        return incidenciaRepositoryPort.findAll();
    }
}
