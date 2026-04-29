package com.company.backendinc.incidencia.application;

import com.company.backendinc.incidencia.Incidencia;
import com.company.backendinc.incidencia.application.port.out.IncidenciaRepositoryPort;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Application use case for creating incidencias with default domain values.
 */
@Service
public class CreateIncidenciaUseCase {
    private final IncidenciaRepositoryPort incidenciaRepositoryPort;

    public CreateIncidenciaUseCase(IncidenciaRepositoryPort incidenciaRepositoryPort) {
        this.incidenciaRepositoryPort = incidenciaRepositoryPort;
    }

    public Incidencia execute(Incidencia payload) {
        String id = payload.getId() != null ? payload.getId() : UUID.randomUUID().toString();
        String estado = payload.getEstado() != null ? payload.getEstado() : "ABIERTA";
        String creadaEn = payload.getCreadaEn() != null ? payload.getCreadaEn() : Instant.now().toString();
        Incidencia created = new Incidencia(
                id,
                payload.getAsunto(),
                payload.getDescripcion(),
                payload.getEmailSolicitante(),
                payload.getPrioridad(),
                estado,
                creadaEn
        );
        return incidenciaRepositoryPort.save(created);
    }
}
