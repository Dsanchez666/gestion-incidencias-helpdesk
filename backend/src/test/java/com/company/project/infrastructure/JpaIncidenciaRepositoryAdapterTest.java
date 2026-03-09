package com.company.project.infrastructure;

import com.company.project.application.command.CreateIncidenciaCommand;
import com.company.project.domain.model.Prioridad;
import com.company.project.domain.port.in.CreateIncidenciaUseCase;
import com.company.project.domain.port.in.ListIncidenciasUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@Testcontainers
class JpaIncidenciaRepositoryAdapterTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private CreateIncidenciaUseCase createIncidenciaUseCase;

    @Autowired
    private ListIncidenciasUseCase listIncidenciasUseCase;

    @Test
    void shouldPersistAndListIncidencias() {
        createIncidenciaUseCase.execute(new CreateIncidenciaCommand(
                "Outlook no abre",
                "Fallo al abrir correo del helpdesk",
                "tecnico@etna.com",
                Prioridad.MEDIA
        ));

        assertFalse(listIncidenciasUseCase.execute().isEmpty());
    }
}
