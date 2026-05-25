package com.company.backendinc.tecnico;

import com.company.backendinc.tecnico.application.ListTecnicosActivosUseCase;
import com.company.backendinc.tecnico.adapter.out.TecnicoRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<String> create(@RequestBody TecnicoCreateRequest request) {
        try {
            if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El nombre del técnico es requerido");
            }
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El email del técnico es requerido");
            }
            tecnicoRepository.create(request.getNombre(), request.getEmail(), request.getPassword());
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error al crear técnico: " + ex.getMessage());
        }
    }
}
