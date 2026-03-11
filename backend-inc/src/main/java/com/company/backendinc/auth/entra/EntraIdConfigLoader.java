package com.company.backendinc.auth.entra;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.stereotype.Component;

@Component
public class EntraIdConfigLoader {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EntraIdConfig load() throws IOException {
        Path configPath = Paths.get("..", "frontend", "src", "assets", "EntraID_Conf.json");
        if (!Files.exists(configPath)) {
            throw new IOException("No se encuentra EntraID_Conf.json en " + configPath.toAbsolutePath());
        }

        String json = Files.readString(configPath);
        return objectMapper.readValue(json, EntraIdConfig.class);
    }
}
