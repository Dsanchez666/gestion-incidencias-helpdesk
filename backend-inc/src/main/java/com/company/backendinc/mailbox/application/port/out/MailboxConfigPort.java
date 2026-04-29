package com.company.backendinc.mailbox.application.port.out;

import com.company.backendinc.mailbox.config.MailboxConfig;
import java.io.IOException;

/**
 * Output port for mailbox configuration retrieval.
 */
public interface MailboxConfigPort {
    MailboxConfig load() throws IOException;
}
