package com.company.project.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataIncidenciaRepository extends JpaRepository<IncidenciaJpaEntity, String> {
}
