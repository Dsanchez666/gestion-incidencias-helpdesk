package com.company.backendinc.mailbox.connection;

import com.company.backendinc.mailbox.config.MailboxConfig;
import com.company.backendinc.mailbox.config.MailboxConfigLoader;
import java.io.IOException;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mailboxes")
public class MailboxConnectionController {
    private final MailboxConfigLoader configLoader;

    public MailboxConnectionController(MailboxConfigLoader configLoader) {
        this.configLoader = configLoader;
    }

    @PostMapping("/graph/test")
    public ResponseEntity<List<ConnectionResult>> testGraph(@RequestHeader("Authorization") String authHeader) {
        try {
            MailboxConfig config = configLoader.load();
            GraphConnectionTester tester = new GraphConnectionTester();
            return ResponseEntity.ok(tester.test(config, authHeader));
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/exchange/test")
    public ResponseEntity<List<ConnectionResult>> testExchange(@RequestHeader("Authorization") String authHeader) {
        try {
            MailboxConfig config = configLoader.load();
            ExchangeConnectionTester tester = new ExchangeConnectionTester();
            return ResponseEntity.ok(tester.test(config, authHeader));
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
