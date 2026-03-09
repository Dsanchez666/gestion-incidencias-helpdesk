package com.company.project.domain.port.in;

import com.company.project.application.command.CreateIncidenciaCommand;
import com.company.project.domain.model.Incidencia;

public interface CreateIncidenciaUseCase {
    Incidencia execute(CreateIncidenciaCommand command);
}
