package com.company.backendinc.incidencia;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidencias")
public class IncidenciaController {
    private static final CopyOnWriteArrayList<Incidencia> STORE = new CopyOnWriteArrayList<>();

    @GetMapping
    public List<Incidencia> list() {
        return STORE;
    }

    @PostMapping
    public Incidencia create(@RequestBody Incidencia payload) {
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
        STORE.add(created);
        return created;
    }
}
