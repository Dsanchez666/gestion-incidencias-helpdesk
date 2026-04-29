package com.company.backendinc.mailbox.application;

import com.company.backendinc.mailbox.Mailbox;
import com.company.backendinc.mailbox.application.port.out.MailboxConfigPort;
import com.company.backendinc.mailbox.config.MailboxConfig;
import com.company.backendinc.mailbox.config.MailboxEntry;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Application use case that returns configured mailboxes.
 */
@Service
public class ListMailboxesUseCase {
    private static final Logger log = LoggerFactory.getLogger(ListMailboxesUseCase.class);
    private final MailboxConfigPort mailboxConfigPort;

    public ListMailboxesUseCase(MailboxConfigPort mailboxConfigPort) {
        this.mailboxConfigPort = mailboxConfigPort;
    }

    public List<Mailbox> execute() {
        try {
            MailboxConfig config = mailboxConfigPort.load();
            if (config.getMailboxes() == null) {
                return Collections.emptyList();
            }
            return config.getMailboxes().stream()
                    .map(this::toMailbox)
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            log.error("Mailbox use case: error reading mailbox configuration", ex);
            return Collections.emptyList();
        }
    }

    private Mailbox toMailbox(MailboxEntry entry) {
        return new Mailbox(entry.getId(), entry.getNombre(), entry.getDireccionCorreo());
    }
}
