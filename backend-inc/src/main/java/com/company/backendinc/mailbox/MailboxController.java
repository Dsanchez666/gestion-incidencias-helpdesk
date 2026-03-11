package com.company.backendinc.mailbox;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/buzones")
public class MailboxController {
    private static final List<Mailbox> MAILBOXES = List.of(
            new Mailbox("1", "Soporte General", "imap.empresa.local", "soporte@empresa.local"),
            new Mailbox("2", "Incidencias TI", "imap.empresa.local", "ti@empresa.local")
    );

    @GetMapping
    public List<Mailbox> list() {
        return MAILBOXES;
    }
}
