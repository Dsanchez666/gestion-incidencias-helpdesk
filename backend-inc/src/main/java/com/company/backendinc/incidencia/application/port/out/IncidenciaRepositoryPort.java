package com.company.backendinc.incidencia.application.port.out;

import com.company.backendinc.incidencia.Incidencia;
import java.util.List;

/**
 * Output port for incidencia persistence.
 */
public interface IncidenciaRepositoryPort {
    List<Incidencia> findAll();

    Incidencia save(Incidencia incidencia);
}
