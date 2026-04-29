package com.company.backendinc.mailbox.adapter.out;

import com.company.backendinc.mailbox.application.port.out.ExchangeConnectionPort;
import com.company.backendinc.mailbox.config.MailboxConfig;
import com.company.backendinc.mailbox.connection.ConnectionResult;
import com.company.backendinc.mailbox.connection.ExchangeConnectionTester;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Outbound adapter for Exchange mailbox connectivity tests.
 */
@Component
public class ExchangeConnectionAdapter implements ExchangeConnectionPort {
    private final ExchangeConnectionTester tester = new ExchangeConnectionTester();

    @Override
    public List<ConnectionResult> test(MailboxConfig config, String authHeader) {
        return tester.test(config, authHeader);
    }
}
