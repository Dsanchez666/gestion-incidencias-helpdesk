package com.company.backendinc.prioridad.application;

import com.company.backendinc.prioridad.adapter.out.Prioridad;
import com.company.backendinc.prioridad.adapter.out.PrioridadRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListPrioridadesUseCase {
    private final PrioridadRepository prioridadRepository;

    public ListPrioridadesUseCase(PrioridadRepository prioridadRepository) {
        this.prioridadRepository = prioridadRepository;
    }

    public List<Prioridad> execute() {
        return prioridadRepository.list();
    }
}
