package com.company.backendinc.auth.entra;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EntraIdMockConfigLoader {
    private static final Logger log = LoggerFactory.getLogger(EntraIdMockConfigLoader.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EntraIdConfig load() throws IOException {
        Path configPath = Paths.get("..", "frontend", "src", "assets", "EntraID_Conf.json");
        log.info("EntraID mock config path: {}", configPath.toAbsolutePath());
        if (!Files.exists(configPath)) {
            throw new IOException("No se encuentra EntraID_Conf.json en " + configPath.toAbsolutePath());
        }

        String json = Files.readString(configPath);
        log.info("EntraID mock config size: {} bytes", json.length());
        return objectMapper.readValue(json, EntraIdConfig.class);
    }
}
