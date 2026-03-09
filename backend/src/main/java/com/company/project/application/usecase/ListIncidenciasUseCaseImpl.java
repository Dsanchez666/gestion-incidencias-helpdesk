package com.company.project.application.usecase;

import com.company.project.domain.model.Incidencia;
import com.company.project.domain.port.in.ListIncidenciasUseCase;
import com.company.project.domain.port.out.SaveIncidenciaPort;

import java.util.List;
import java.util.Objects;

public class ListIncidenciasUseCaseImpl implements ListIncidenciasUseCase {

    private final SaveIncidenciaPort saveIncidenciaPort;

    public ListIncidenciasUseCaseImpl(SaveIncidenciaPort saveIncidenciaPort) {
        this.saveIncidenciaPort = Objects.requireNonNull(saveIncidenciaPort, "saveIncidenciaPort is required");
    }

    @Override
    public List<Incidencia> execute() {
        return saveIncidenciaPort.findAll();
    }
}
