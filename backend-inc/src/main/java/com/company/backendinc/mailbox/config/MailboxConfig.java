package com.company.backendinc.mailbox.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MailboxConfig {
    private String graphBaseUrl;
    private String exchangeEwsUrl;
    private SmtpConfig smtp;
    private List<MailboxEntry> mailboxes;

    public String getGraphBaseUrl() {
        return graphBaseUrl;
    }

    public void setGraphBaseUrl(String graphBaseUrl) {
        this.graphBaseUrl = graphBaseUrl;
    }

    public String getExchangeEwsUrl() {
        return exchangeEwsUrl;
    }

    public void setExchangeEwsUrl(String exchangeEwsUrl) {
        this.exchangeEwsUrl = exchangeEwsUrl;
    }

    public SmtpConfig getSmtp() {
        return smtp;
    }

    public void setSmtp(SmtpConfig smtp) {
        this.smtp = smtp;
    }

    public List<MailboxEntry> getMailboxes() {
        return mailboxes;
    }

    public void setMailboxes(List<MailboxEntry> mailboxes) {
        this.mailboxes = mailboxes;
    }
}
