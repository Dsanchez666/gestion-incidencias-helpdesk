package com.company.backendinc.mailbox.adapter.out;

import com.company.backendinc.mailbox.application.port.out.GraphConnectionPort;
import com.company.backendinc.mailbox.config.MailboxConfig;
import com.company.backendinc.mailbox.connection.ConnectionResult;
import com.company.backendinc.mailbox.connection.GraphConnectionTester;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Outbound adapter for Graph mailbox connectivity tests.
 */
@Component
public class GraphConnectionAdapter implements GraphConnectionPort {
    private final GraphConnectionTester tester = new GraphConnectionTester();

    @Override
    public List<ConnectionResult> test(MailboxConfig config, String authHeader) {
        return tester.test(config, authHeader);
    }
}
