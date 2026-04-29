package com.company.backendinc.mailbox;

import com.company.backendinc.mailbox.application.ListMailboxesUseCase;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/buzones")
public class MailboxController {
    private static final Logger log = LoggerFactory.getLogger(MailboxController.class);
    private final ListMailboxesUseCase listMailboxesUseCase;

    public MailboxController(ListMailboxesUseCase listMailboxesUseCase) {
        this.listMailboxesUseCase = listMailboxesUseCase;
    }

    @GetMapping
    public List<Mailbox> list() {
        log.info("Buzones: recibido GET /api/buzones");
        List<Mailbox> result = listMailboxesUseCase.execute();
        log.info("Buzones: {} buzones cargados", result.size());
        return result;
    }
}
