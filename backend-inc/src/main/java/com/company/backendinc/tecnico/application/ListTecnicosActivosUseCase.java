package com.company.backendinc.tecnico.application;

import com.company.backendinc.tecnico.Tecnico;
import com.company.backendinc.tecnico.adapter.out.TecnicoRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListTecnicosActivosUseCase {
    private final TecnicoRepository tecnicoRepository;

    public ListTecnicosActivosUseCase(TecnicoRepository tecnicoRepository) {
        this.tecnicoRepository = tecnicoRepository;
    }

    public List<Tecnico> execute() {
        return tecnicoRepository.findActivos();
    }
}
