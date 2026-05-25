package com.company.backendinc.prioridad.adapter.in;

import com.company.backendinc.prioridad.adapter.out.Prioridad;
import com.company.backendinc.prioridad.application.ListPrioridadesUseCase;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prioridades")
public class PrioridadController {
    private final ListPrioridadesUseCase listPrioridadesUseCase;

    public PrioridadController(ListPrioridadesUseCase listPrioridadesUseCase) {
        this.listPrioridadesUseCase = listPrioridadesUseCase;
    }

    @GetMapping
    public List<Prioridad> list() {
        return listPrioridadesUseCase.execute();
    }
}
