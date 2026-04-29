package com.company.backendinc.mailbox.application;

import com.company.backendinc.mailbox.application.port.out.GraphConnectionPort;
import com.company.backendinc.mailbox.application.port.out.MailboxConfigPort;
import com.company.backendinc.mailbox.connection.ConnectionResult;
import com.company.backendinc.mailbox.config.MailboxConfig;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Application use case for Graph connection checks.
 */
@Service
public class TestGraphConnectionUseCase {
    private final MailboxConfigPort mailboxConfigPort;
    private final GraphConnectionPort graphConnectionPort;

    public TestGraphConnectionUseCase(MailboxConfigPort mailboxConfigPort, GraphConnectionPort graphConnectionPort) {
        this.mailboxConfigPort = mailboxConfigPort;
        this.graphConnectionPort = graphConnectionPort;
    }

    public List<ConnectionResult> execute(String authHeader) throws IOException {
        MailboxConfig config = mailboxConfigPort.load();
        return graphConnectionPort.test(config, authHeader);
    }
}
