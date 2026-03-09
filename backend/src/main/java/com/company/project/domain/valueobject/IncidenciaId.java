package com.company.project.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record IncidenciaId(String value) {
    public IncidenciaId {
        Objects.requireNonNull(value, "id value is required");
        UUID.fromString(value);
    }

    public static IncidenciaId nueva() {
        return new IncidenciaId(UUID.randomUUID().toString());
    }
}
