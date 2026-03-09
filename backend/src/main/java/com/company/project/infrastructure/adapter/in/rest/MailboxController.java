package com.company.project.infrastructure.adapter.in.rest;

import com.company.project.infrastructure.adapter.in.rest.dto.mailbox.MailboxResponse;
import com.company.project.infrastructure.config.HelpdeskMailboxesProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/buzones")
public class MailboxController {

    private final HelpdeskMailboxesProperties mailboxesProperties;

    public MailboxController(HelpdeskMailboxesProperties mailboxesProperties) {
        this.mailboxesProperties = mailboxesProperties;
    }

    @GetMapping
    public List<MailboxResponse> list() {
        return mailboxesProperties.getMailboxes().stream()
                .map(mailbox -> new MailboxResponse(
                        mailbox.getId(),
                        mailbox.getNombre(),
                        mailbox.getServidorImap(),
                        mailbox.getUsuario()
                ))
                .toList();
    }
}
