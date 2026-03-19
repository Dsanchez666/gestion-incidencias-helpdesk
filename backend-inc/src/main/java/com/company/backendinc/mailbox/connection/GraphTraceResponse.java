package com.company.backendinc.mailbox.connection;

import java.util.List;

public class GraphTraceResponse {
    private boolean success;
    private List<String> traces;
    private List<MailboxFolderResult> mailboxes;
    private String error;

    public GraphTraceResponse() {
    }

    public GraphTraceResponse(boolean success, List<String> traces, List<MailboxFolderResult> mailboxes, String error) {
        this.success = success;
        this.traces = traces;
        this.mailboxes = mailboxes;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<String> getTraces() {
        return traces;
    }

    public void setTraces(List<String> traces) {
        this.traces = traces;
    }

    public List<MailboxFolderResult> getMailboxes() {
        return mailboxes;
    }

    public void setMailboxes(List<MailboxFolderResult> mailboxes) {
        this.mailboxes = mailboxes;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
