package com.company.backendinc.inbox;

import com.company.backendinc.inbox.application.InboxManagementUseCase;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inbox/gestion")
public class InboxManagementController {
    private final InboxManagementUseCase useCase;

    public InboxManagementController(InboxManagementUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public ResponseEntity<List<InboxItem>> list(@RequestParam(name = "summaryLength", defaultValue = "50") int summaryLength)
            throws IOException {
        return ResponseEntity.ok(useCase.listInbox(summaryLength));
    }

    @GetMapping("/context")
    public ResponseEntity<InboxContext> context() throws IOException {
        return ResponseEntity.ok(useCase.getContext());
    }

    @GetMapping("/incidencias")
    public ResponseEntity<List<IncidenciaInboxItem>> incidencias() {
        return ResponseEntity.ok(useCase.listIncidencias());
    }

    @GetMapping("/incidencias/{incidenciaId}/notas")
    public ResponseEntity<List<IncidenciaNota>> notas(@PathVariable Long incidenciaId) {
        return ResponseEntity.ok(useCase.listNotas(incidenciaId));
    }

    @PatchMapping("/{messageId}/incidencia")
    public ResponseEntity<Void> updateIncidencia(@PathVariable String messageId,
            @RequestBody IncidenciaUpdateRequest request) {
        useCase.updateIncidencia(messageId, request.isIncidenciaGenerada());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{messageId}/asignacion")
    public ResponseEntity<Void> updateAsignacion(@PathVariable String messageId,
            @RequestBody AsignacionUpdateRequest request) {
        useCase.updateAsignacion(messageId, request.isAsignada(), request.getTecnicoAsignado());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{messageId}/asignar-incidencia")
    public ResponseEntity<Void> assignIncidencia(@PathVariable String messageId,
            @RequestParam(name = "summaryLength", defaultValue = "50") int summaryLength,
            @RequestBody AsignarIncidenciaRequest request) throws IOException {
        try {
            useCase.assignIncidencia(messageId, request.getTecnicoNombre(), summaryLength);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/asignar-incidencias")
    public ResponseEntity<Void> assignIncidencias(
            @RequestParam(name = "summaryLength", defaultValue = "50") int summaryLength,
            @RequestBody AsignarIncidenciasRequest request) throws IOException {
        try {
            useCase.assignIncidencias(request.getMessageIds(), request.getTecnicoNombre(), summaryLength);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PatchMapping("/incidencias/{incidenciaId}/categoria")
    public ResponseEntity<Void> updateCategoria(@PathVariable Long incidenciaId, @RequestBody CategoriaUpdateRequest request) {
        try {
            useCase.updateCategoria(incidenciaId, request.getCategoriaId());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PatchMapping("/incidencias/{incidenciaId}/tecnico")
    public ResponseEntity<Void> updateTecnico(@PathVariable Long incidenciaId, @RequestBody TecnicoIncidenciaUpdateRequest request) {
        try {
            useCase.updateTecnicoIncidencia(incidenciaId, request.getTecnicoNombre());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PatchMapping("/incidencias/{incidenciaId}/resuelta")
    public ResponseEntity<Void> updateResuelta(@PathVariable Long incidenciaId, @RequestBody ResueltaUpdateRequest request) {
        useCase.updateResuelta(incidenciaId, request.isResuelta());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/incidencias/stats")
    public ResponseEntity<IncidenciasStatsResponse> stats() {
        return ResponseEntity.ok(useCase.getStats());
    }

    @PostMapping("/incidencias/{incidenciaId}/notas")
    public ResponseEntity<Void> addNota(@PathVariable Long incidenciaId, @RequestBody IncidenciaNotaCreateRequest request) {
        useCase.addNota(incidenciaId, request.getTecnico(), request.getObservacion(), request.getDetalle(), request.getAccionRealizada());
        return ResponseEntity.noContent().build();
    }
}
