package com.company.backendinc.mailbox;

import com.company.backendinc.mailbox.config.MailboxConfig;
import com.company.backendinc.mailbox.config.MailboxConfigLoader;
import com.company.backendinc.mailbox.config.MailboxEntry;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/buzones")
public class MailboxController {
    private final MailboxConfigLoader configLoader;

    public MailboxController(MailboxConfigLoader configLoader) {
        this.configLoader = configLoader;
    }

    @GetMapping
    public List<Mailbox> list() {
        try {
            MailboxConfig config = configLoader.load();
            if (config.getMailboxes() == null) {
                return Collections.emptyList();
            }
            return config.getMailboxes().stream()
                    .map(this::toMailbox)
                    .collect(Collectors.toList());
        } catch (IOException ex) {
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
