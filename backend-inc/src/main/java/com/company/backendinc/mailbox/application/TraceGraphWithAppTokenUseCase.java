package com.company.backendinc.mailbox.application;

import com.company.backendinc.mailbox.application.port.out.GraphTracePort;
import com.company.backendinc.mailbox.connection.GraphTraceResponse;
import org.springframework.stereotype.Service;

/**
 * Application use case for Graph tracing with app-to-app token.
 */
@Service
public class TraceGraphWithAppTokenUseCase {
    private final GraphTracePort graphTracePort;

    public TraceGraphWithAppTokenUseCase(GraphTracePort graphTracePort) {
        this.graphTracePort = graphTracePort;
    }

    public GraphTraceResponse execute() {
        return graphTracePort.traceWithAppToken();
    }
}
