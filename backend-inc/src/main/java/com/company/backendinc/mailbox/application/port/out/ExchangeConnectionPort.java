package com.company.backendinc.mailbox.application.port.out;

import com.company.backendinc.mailbox.config.MailboxConfig;
import com.company.backendinc.mailbox.connection.ConnectionResult;
import java.util.List;

/**
 * Output port for mailbox connectivity checks against Exchange.
 */
public interface ExchangeConnectionPort {
    List<ConnectionResult> test(MailboxConfig config, String authHeader);
}
