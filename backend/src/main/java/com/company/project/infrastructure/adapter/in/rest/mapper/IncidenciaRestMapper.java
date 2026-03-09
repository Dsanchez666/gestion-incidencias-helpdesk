package com.company.project.infrastructure.adapter.in.rest.mapper;

import com.company.project.application.command.CreateIncidenciaCommand;
import com.company.project.domain.model.Incidencia;
import com.company.project.infrastructure.adapter.in.rest.dto.CreateIncidenciaRequest;
import com.company.project.infrastructure.adapter.in.rest.dto.IncidenciaResponse;

public class IncidenciaRestMapper {

    public CreateIncidenciaCommand toCommand(CreateIncidenciaRequest request) {
        return new CreateIncidenciaCommand(
                request.asunto(),
                request.descripcion(),
                request.emailSolicitante(),
                request.prioridad()
        );
    }

    public IncidenciaResponse toResponse(Incidencia incidencia) {
        return new IncidenciaResponse(
                incidencia.id().value(),
                incidencia.asunto(),
                incidencia.descripcion(),
                incidencia.emailSolicitante(),
                incidencia.prioridad(),
                incidencia.estado(),
                incidencia.creadaEn()
        );
    }
}
