package com.company.backendinc.tecnico;

import com.company.backendinc.tecnico.application.ListTecnicosActivosUseCase;
import com.company.backendinc.tecnico.adapter.out.TecnicoRepository;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tecnicos")
public class TecnicoController {
    private final ListTecnicosActivosUseCase listTecnicosActivosUseCase;
    private final TecnicoRepository tecnicoRepository;

    public TecnicoController(ListTecnicosActivosUseCase listTecnicosActivosUseCase, TecnicoRepository tecnicoRepository) {
        this.listTecnicosActivosUseCase = listTecnicosActivosUseCase;
        this.tecnicoRepository = tecnicoRepository;
    }

    @GetMapping
    public List<Tecnico> listActivos() {
        return listTecnicosActivosUseCase.execute();
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody TecnicoCreateRequest request) {
        tecnicoRepository.create(request.getNombre(), request.getEmail());
        return ResponseEntity.noContent().build();
    }
}
