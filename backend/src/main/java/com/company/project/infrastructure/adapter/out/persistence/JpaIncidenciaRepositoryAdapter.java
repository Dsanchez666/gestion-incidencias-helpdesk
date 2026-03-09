package com.company.project.infrastructure.adapter.out.persistence;

import com.company.project.domain.model.Incidencia;
import com.company.project.domain.port.out.SaveIncidenciaPort;
import com.company.project.domain.valueobject.IncidenciaId;

import java.util.List;

public class JpaIncidenciaRepositoryAdapter implements SaveIncidenciaPort {

    private final SpringDataIncidenciaRepository repository;

    public JpaIncidenciaRepositoryAdapter(SpringDataIncidenciaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Incidencia save(Incidencia incidencia) {
        IncidenciaJpaEntity entity = new IncidenciaJpaEntity(
                incidencia.id().value(),
                incidencia.asunto(),
                incidencia.descripcion(),
                incidencia.emailSolicitante(),
                incidencia.prioridad(),
                incidencia.estado(),
                incidencia.creadaEn()
        );
        IncidenciaJpaEntity saved = repository.save(entity);
        return map(saved);
    }

    @Override
    public List<Incidencia> findAll() {
        return repository.findAll().stream().map(this::map).toList();
    }

    private Incidencia map(IncidenciaJpaEntity entity) {
        return Incidencia.rehydrate(
                new IncidenciaId(entity.getId()),
                entity.getAsunto(),
                entity.getDescripcion(),
                entity.getEmailSolicitante(),
                entity.getPrioridad(),
                entity.getEstado(),
                entity.getCreadaEn()
        );
    }
}
