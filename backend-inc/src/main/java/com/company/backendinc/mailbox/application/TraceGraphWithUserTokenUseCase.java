package com.company.backendinc.mailbox.application;

import com.company.backendinc.mailbox.application.port.out.GraphTracePort;
import com.company.backendinc.mailbox.connection.GraphTraceResponse;
import org.springframework.stereotype.Service;

/**
 * Application use case for Graph tracing with interactive user token.
 */
@Service
public class TraceGraphWithUserTokenUseCase {
    private final GraphTracePort graphTracePort;

    public TraceGraphWithUserTokenUseCase(GraphTracePort graphTracePort) {
        this.graphTracePort = graphTracePort;
    }

    public GraphTraceResponse execute() {
        return graphTracePort.traceWithUserToken();
    }
}
