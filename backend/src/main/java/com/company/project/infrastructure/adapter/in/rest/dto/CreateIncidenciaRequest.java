package com.company.project.infrastructure.adapter.in.rest.dto;

import com.company.project.domain.model.Prioridad;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateIncidenciaRequest(
        @NotBlank @Size(min = 3, max = 120) String asunto,
        @NotBlank @Size(min = 5, max = 2000) String descripcion,
        @NotBlank @Email String emailSolicitante,
        @NotNull Prioridad prioridad
) {
}
