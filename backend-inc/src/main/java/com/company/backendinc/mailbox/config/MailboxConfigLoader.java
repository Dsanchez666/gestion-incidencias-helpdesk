package com.company.backendinc.mailbox.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MailboxConfigLoader {
    private static final Logger log = LoggerFactory.getLogger(MailboxConfigLoader.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MailboxConfig load() throws IOException {
        Path configPath = Paths.get("..", "frontend", "src", "assets", "Mailboxes_Conf.json");
        log.info("Buzones: leyendo config desde {}", configPath.toAbsolutePath());
        if (!Files.exists(configPath)) {
            log.error("Buzones: no se encuentra Mailboxes_Conf.json");
            throw new IOException("No se encuentra Mailboxes_Conf.json en " + configPath.toAbsolutePath());
        }

        String json = Files.readString(configPath);
        log.info("Buzones: config leida, tamanio {} bytes", json.length());
        return objectMapper.readValue(json, MailboxConfig.class);
    }
}
