package com.company.backendinc.mailbox.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.stereotype.Component;

@Component
public class MailboxConfigLoader {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MailboxConfig load() throws IOException {
        Path configPath = Paths.get("..", "frontend", "src", "assets", "Mailboxes_Conf.json");
        if (!Files.exists(configPath)) {
            throw new IOException("No se encuentra Mailboxes_Conf.json en " + configPath.toAbsolutePath());
        }

        String json = Files.readString(configPath);
        return objectMapper.readValue(json, MailboxConfig.class);
    }
}
