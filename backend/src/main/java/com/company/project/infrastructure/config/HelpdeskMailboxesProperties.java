package com.company.project.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "helpdesk")
public class HelpdeskMailboxesProperties {

    private List<Mailbox> mailboxes = new ArrayList<>();

    public List<Mailbox> getMailboxes() {
        return mailboxes;
    }

    public void setMailboxes(List<Mailbox> mailboxes) {
        this.mailboxes = mailboxes;
    }

    public static class Mailbox {
        @NotBlank
        private String id;

        @NotBlank
        private String nombre;

        @NotBlank
        private String servidorImap;

        @NotBlank
        private String usuario;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getServidorImap() {
            return servidorImap;
        }

        public void setServidorImap(String servidorImap) {
            this.servidorImap = servidorImap;
        }

        public String getUsuario() {
            return usuario;
        }

        public void setUsuario(String usuario) {
            this.usuario = usuario;
        }
    }
}
