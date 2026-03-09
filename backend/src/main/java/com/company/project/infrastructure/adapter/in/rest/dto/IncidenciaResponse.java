package com.company.project.infrastructure.adapter.in.rest.dto;

import com.company.project.domain.model.Estado;
import com.company.project.domain.model.Prioridad;

import java.time.Instant;

public record IncidenciaResponse(
        String id,
        String asunto,
        String descripcion,
        String emailSolicitante,
        Prioridad prioridad,
        Estado estado,
        Instant creadaEn
) {
}
