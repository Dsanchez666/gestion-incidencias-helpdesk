package com.company.backendinc.tecnico;

import com.company.backendinc.tecnico.application.ListTecnicosActivosUseCase;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tecnicos")
public class TecnicoController {
    private final ListTecnicosActivosUseCase listTecnicosActivosUseCase;

    public TecnicoController(ListTecnicosActivosUseCase listTecnicosActivosUseCase) {
        this.listTecnicosActivosUseCase = listTecnicosActivosUseCase;
    }

    @GetMapping
    public List<Tecnico> listActivos() {
        return listTecnicosActivosUseCase.execute();
    }
}
