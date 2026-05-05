package com.company.backendinc.inbox;

import com.company.backendinc.inbox.application.InboxManagementUseCase;
import java.io.IOException;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}
