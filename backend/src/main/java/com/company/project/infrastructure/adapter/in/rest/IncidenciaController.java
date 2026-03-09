package com.company.project.infrastructure.adapter.in.rest;

import com.company.project.domain.port.in.CreateIncidenciaUseCase;
import com.company.project.domain.port.in.ListIncidenciasUseCase;
import com.company.project.infrastructure.adapter.in.rest.dto.CreateIncidenciaRequest;
import com.company.project.infrastructure.adapter.in.rest.dto.IncidenciaResponse;
import com.company.project.infrastructure.adapter.in.rest.mapper.IncidenciaRestMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/incidencias")
public class IncidenciaController {

    private final CreateIncidenciaUseCase createIncidenciaUseCase;
    private final ListIncidenciasUseCase listIncidenciasUseCase;
    private final IncidenciaRestMapper mapper;

    public IncidenciaController(
            CreateIncidenciaUseCase createIncidenciaUseCase,
            ListIncidenciasUseCase listIncidenciasUseCase,
            IncidenciaRestMapper mapper
    ) {
        this.createIncidenciaUseCase = createIncidenciaUseCase;
        this.listIncidenciasUseCase = listIncidenciasUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncidenciaResponse create(@Valid @RequestBody CreateIncidenciaRequest request) {
        return mapper.toResponse(createIncidenciaUseCase.execute(mapper.toCommand(request)));
    }

    @GetMapping
    public List<IncidenciaResponse> list() {
        return listIncidenciasUseCase.execute().stream().map(mapper::toResponse).toList();
    }
}
