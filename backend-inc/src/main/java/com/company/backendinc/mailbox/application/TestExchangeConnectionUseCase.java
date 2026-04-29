package com.company.backendinc.mailbox.application;

import com.company.backendinc.mailbox.application.port.out.ExchangeConnectionPort;
import com.company.backendinc.mailbox.application.port.out.MailboxConfigPort;
import com.company.backendinc.mailbox.connection.ConnectionResult;
import com.company.backendinc.mailbox.config.MailboxConfig;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Application use case for Exchange connection checks.
 */
@Service
public class TestExchangeConnectionUseCase {
    private final MailboxConfigPort mailboxConfigPort;
    private final ExchangeConnectionPort exchangeConnectionPort;

    public TestExchangeConnectionUseCase(MailboxConfigPort mailboxConfigPort,
            ExchangeConnectionPort exchangeConnectionPort) {
        this.mailboxConfigPort = mailboxConfigPort;
        this.exchangeConnectionPort = exchangeConnectionPort;
    }

    public List<ConnectionResult> execute(String authHeader) throws IOException {
        MailboxConfig config = mailboxConfigPort.load();
        return exchangeConnectionPort.test(config, authHeader);
    }
}
