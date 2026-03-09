package com.company.project.infrastructure.adapter.in.rest.dto.mailbox;

public record MailboxResponse(
        String id,
        String nombre,
        String servidorImap,
        String usuario
) {
}
