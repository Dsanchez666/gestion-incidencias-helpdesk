package com.company.backendinc.mailbox.application.port.out;

import com.company.backendinc.mailbox.connection.GraphTraceResponse;

/**
 * Output port for Graph tracing scenarios.
 */
public interface GraphTracePort {
    GraphTraceResponse traceWithAppToken();

    GraphTraceResponse traceWithUserToken();
}
