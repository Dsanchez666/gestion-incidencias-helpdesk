package com.company.backendinc.mailbox;

import com.company.backendinc.mailbox.config.MailboxConfig;
import com.company.backendinc.mailbox.config.MailboxConfigLoader;
import com.company.backendinc.mailbox.config.MailboxEntry;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/buzones")
public class MailboxController {
    private static final Logger log = LoggerFactory.getLogger(MailboxController.class);
    private final MailboxConfigLoader configLoader;

    public MailboxController(MailboxConfigLoader configLoader) {
        this.configLoader = configLoader;
    }

    @GetMapping
    public List<Mailbox> list() {
        log.info("Buzones: recibido GET /api/buzones");
        try {
            MailboxConfig config = configLoader.load();
            if (config.getMailboxes() == null) {
                log.info("Buzones: configuracion sin lista de buzones");
                return Collections.emptyList();
            }
            List<Mailbox> result = config.getMailboxes().stream()
                    .map(this::toMailbox)
                    .collect(Collectors.toList());
            log.info("Buzones: {} buzones cargados", result.size());
            return result;
        } catch (IOException ex) {
            log.error("Buzones: error leyendo configuracion", ex);
            return Collections.emptyList();
        }
    }

    private Mailbox toMailbox(MailboxEntry entry) {
        return new Mailbox(
                entry.getId(),
                entry.getNombre(),
                entry.getDireccionCorreo()
        );
    }
}
