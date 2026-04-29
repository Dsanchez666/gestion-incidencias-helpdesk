package com.company.backendinc.mailbox.connection;

import com.company.backendinc.mailbox.application.TestExchangeConnectionUseCase;
import com.company.backendinc.mailbox.application.TestGraphConnectionUseCase;
import com.company.backendinc.mailbox.application.TraceGraphWithAppTokenUseCase;
import com.company.backendinc.mailbox.application.TraceGraphWithUserTokenUseCase;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Input adapter exposing mailbox connection and tracing endpoints.
 */
@RestController
@RequestMapping("/api/mailboxes")
public class MailboxConnectionController {
    private static final Logger log = LoggerFactory.getLogger(MailboxConnectionController.class);
    private final TestGraphConnectionUseCase testGraphConnectionUseCase;
    private final TestExchangeConnectionUseCase testExchangeConnectionUseCase;
    private final TraceGraphWithAppTokenUseCase traceGraphWithAppTokenUseCase;
    private final TraceGraphWithUserTokenUseCase traceGraphWithUserTokenUseCase;

    public MailboxConnectionController(TestGraphConnectionUseCase testGraphConnectionUseCase,
            TestExchangeConnectionUseCase testExchangeConnectionUseCase,
            TraceGraphWithAppTokenUseCase traceGraphWithAppTokenUseCase,
            TraceGraphWithUserTokenUseCase traceGraphWithUserTokenUseCase) {
        this.testGraphConnectionUseCase = testGraphConnectionUseCase;
        this.testExchangeConnectionUseCase = testExchangeConnectionUseCase;
        this.traceGraphWithAppTokenUseCase = traceGraphWithAppTokenUseCase;
        this.traceGraphWithUserTokenUseCase = traceGraphWithUserTokenUseCase;
    }

    @PostMapping("/graph/test")
    public ResponseEntity<List<ConnectionResult>> testGraph(@RequestHeader("Authorization") String authHeader) {
        log.info("MailboxController: /graph/test authScheme={}", authScheme(authHeader));
        try {
            List<ConnectionResult> response = testGraphConnectionUseCase.execute(authHeader);
            log.info("MailboxController: /graph/test results={}", response.size());
            return ResponseEntity.ok(response);
        } catch (IOException ex) {
            log.error("MailboxController: /graph/test error leyendo config: {}", ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/exchange/test")
    public ResponseEntity<List<ConnectionResult>> testExchange(@RequestHeader("Authorization") String authHeader) {
        log.info("MailboxController: /exchange/test authScheme={}", authScheme(authHeader));
        try {
            List<ConnectionResult> response = testExchangeConnectionUseCase.execute(authHeader);
            log.info("MailboxController: /exchange/test results={}", response.size());
            return ResponseEntity.ok(response);
        } catch (IOException ex) {
            log.error("MailboxController: /exchange/test error leyendo config: {}", ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/graph/trace")
    public ResponseEntity<GraphTraceResponse> traceGraph() {
        log.info("MailboxController: /graph/trace start");
        GraphTraceResponse response = traceGraphWithAppTokenUseCase.execute();
        log.info("MailboxController: /graph/trace end success={} error={}", response.isSuccess(), response.getError());
        return toHttpResponse(response);
    }

    @PostMapping("/graph/user/trace")
    public ResponseEntity<GraphTraceResponse> traceGraphUser() {
        log.info("MailboxController: /graph/user/trace start");
        GraphTraceResponse response = traceGraphWithUserTokenUseCase.execute();
        log.info("MailboxController: /graph/user/trace end success={} error={}", response.isSuccess(), response.getError());
        return toHttpResponse(response);
    }

    private ResponseEntity<GraphTraceResponse> toHttpResponse(GraphTraceResponse response) {
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        if (response.getError() != null && response.getError().contains("Login requerido")) {
            return ResponseEntity.status(401).body(response);
        }
        if (response.getError() != null && (response.getError().contains("no configurado")
                || response.getError().contains("resolver el token url"))) {
            return ResponseEntity.badRequest().body(response);
        }
        if (response.getError() != null && response.getError().contains("leyendo")) {
            return ResponseEntity.internalServerError().body(response);
        }
        return ResponseEntity.status(502).body(response);
    }

    private String authScheme(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            return "none";
        }
        int firstBlank = authHeader.indexOf(' ');
        if (firstBlank <= 0) {
            return "unknown";
        }
        return authHeader.substring(0, firstBlank);
    }
}
