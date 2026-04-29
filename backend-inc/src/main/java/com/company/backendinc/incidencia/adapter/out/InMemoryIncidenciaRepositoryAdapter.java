package com.company.backendinc.incidencia.adapter.out;

import com.company.backendinc.incidencia.Incidencia;
import com.company.backendinc.incidencia.application.port.out.IncidenciaRepositoryPort;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

/**
 * In-memory persistence adapter for incidencias.
 */
@Component
public class InMemoryIncidenciaRepositoryAdapter implements IncidenciaRepositoryPort {
    private final CopyOnWriteArrayList<Incidencia> store = new CopyOnWriteArrayList<>();

    @Override
    public List<Incidencia> findAll() {
        return List.copyOf(store);
    }

    @Override
    public Incidencia save(Incidencia incidencia) {
        store.add(incidencia);
        return incidencia;
    }
}
