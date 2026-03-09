package com.company.project.infrastructure.config;

import com.company.project.application.usecase.CreateIncidenciaUseCaseImpl;
import com.company.project.application.usecase.ListIncidenciasUseCaseImpl;
import com.company.project.domain.port.in.CreateIncidenciaUseCase;
import com.company.project.domain.port.in.ListIncidenciasUseCase;
import com.company.project.domain.port.out.SaveIncidenciaPort;
import com.company.project.infrastructure.adapter.in.rest.mapper.IncidenciaRestMapper;
import com.company.project.infrastructure.adapter.out.persistence.JpaIncidenciaRepositoryAdapter;
import com.company.project.infrastructure.adapter.out.persistence.SpringDataIncidenciaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    SaveIncidenciaPort saveIncidenciaPort(SpringDataIncidenciaRepository repository) {
        return new JpaIncidenciaRepositoryAdapter(repository);
    }

    @Bean
    CreateIncidenciaUseCase createIncidenciaUseCase(SaveIncidenciaPort saveIncidenciaPort) {
        return new CreateIncidenciaUseCaseImpl(saveIncidenciaPort);
    }

    @Bean
    ListIncidenciasUseCase listIncidenciasUseCase(SaveIncidenciaPort saveIncidenciaPort) {
        return new ListIncidenciasUseCaseImpl(saveIncidenciaPort);
    }

    @Bean
    IncidenciaRestMapper incidenciaRestMapper() {
        return new IncidenciaRestMapper();
    }
}
