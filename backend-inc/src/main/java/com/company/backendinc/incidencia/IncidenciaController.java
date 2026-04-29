package com.company.backendinc.incidencia;

import com.company.backendinc.incidencia.application.CreateIncidenciaUseCase;
import com.company.backendinc.incidencia.application.ListIncidenciasUseCase;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidencias")
public class IncidenciaController {
    private final ListIncidenciasUseCase listIncidenciasUseCase;
    private final CreateIncidenciaUseCase createIncidenciaUseCase;

    public IncidenciaController(ListIncidenciasUseCase listIncidenciasUseCase,
            CreateIncidenciaUseCase createIncidenciaUseCase) {
        this.listIncidenciasUseCase = listIncidenciasUseCase;
        this.createIncidenciaUseCase = createIncidenciaUseCase;
    }

    @GetMapping
    public List<Incidencia> list() {
        return listIncidenciasUseCase.execute();
    }

    @PostMapping
    public Incidencia create(@RequestBody Incidencia payload) {
        return createIncidenciaUseCase.execute(payload);
    }
}
