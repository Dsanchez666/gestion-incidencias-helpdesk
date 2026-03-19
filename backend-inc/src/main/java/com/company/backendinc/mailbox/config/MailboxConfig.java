package com.company.backendinc.mailbox.config;

import java.util.List;

public class MailboxConfig {
    private String graphBaseUrl;
    private String exchangeEwsUrl;
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

    public List<MailboxEntry> getMailboxes() {
        return mailboxes;
    }

    public void setMailboxes(List<MailboxEntry> mailboxes) {
        this.mailboxes = mailboxes;
    }
}
