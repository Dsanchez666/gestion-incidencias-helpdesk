package com.company.project.application.command;

import com.company.project.domain.model.Prioridad;

public record CreateIncidenciaCommand(
        String asunto,
        String descripcion,
        String emailSolicitante,
        Prioridad prioridad
) {
}
