package com.company.project.application.usecase;

import com.company.project.application.command.CreateIncidenciaCommand;
import com.company.project.domain.model.Incidencia;
import com.company.project.domain.port.in.CreateIncidenciaUseCase;
import com.company.project.domain.port.out.SaveIncidenciaPort;
import com.company.project.domain.valueobject.Email;
import com.company.project.domain.valueobject.IncidenciaId;

import java.util.Objects;

public class CreateIncidenciaUseCaseImpl implements CreateIncidenciaUseCase {

    private final SaveIncidenciaPort saveIncidenciaPort;

    public CreateIncidenciaUseCaseImpl(SaveIncidenciaPort saveIncidenciaPort) {
        this.saveIncidenciaPort = Objects.requireNonNull(saveIncidenciaPort, "saveIncidenciaPort is required");
    }

    @Override
    public Incidencia execute(CreateIncidenciaCommand command) {
        Email email = new Email(command.emailSolicitante());
        Incidencia incidencia = Incidencia.nueva(
                IncidenciaId.nueva(),
                command.asunto(),
                command.descripcion(),
                email.value(),
                command.prioridad()
        );
        return saveIncidenciaPort.save(incidencia);
    }
}
