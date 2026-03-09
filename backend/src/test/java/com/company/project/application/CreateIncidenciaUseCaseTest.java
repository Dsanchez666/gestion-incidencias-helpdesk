package com.company.project.application;

import com.company.project.application.command.CreateIncidenciaCommand;
import com.company.project.application.usecase.CreateIncidenciaUseCaseImpl;
import com.company.project.domain.model.Incidencia;
import com.company.project.domain.model.Prioridad;
import com.company.project.domain.port.out.SaveIncidenciaPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateIncidenciaUseCaseTest {

    @Mock
    private SaveIncidenciaPort saveIncidenciaPort;

    @InjectMocks
    private CreateIncidenciaUseCaseImpl useCase;

    @Test
    void shouldCreateIncidenciaWithValidatedEmail() {
        when(saveIncidenciaPort.save(any(Incidencia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateIncidenciaCommand command = new CreateIncidenciaCommand(
                "Error login SSO",
                "No se puede acceder al buzon de ayuda",
                "AGENTE.ETNA@CORP.COM",
                Prioridad.ALTA
        );

        Incidencia created = useCase.execute(command);

        ArgumentCaptor<Incidencia> captor = ArgumentCaptor.forClass(Incidencia.class);
        verify(saveIncidenciaPort).save(captor.capture());
        assertEquals("agente.etna@corp.com", captor.getValue().emailSolicitante());
        assertEquals(Prioridad.ALTA, created.prioridad());
    }
}
