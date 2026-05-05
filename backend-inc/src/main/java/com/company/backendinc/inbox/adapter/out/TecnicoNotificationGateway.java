package com.company.backendinc.inbox.adapter.out;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class TecnicoNotificationGateway {
    private static final Logger log = LoggerFactory.getLogger(TecnicoNotificationGateway.class);
    private final JavaMailSender mailSender;
    private final String from;

    public TecnicoNotificationGateway(JavaMailSender mailSender,
            @Value("${app.notifications.from:no-reply@gestion-incidencias.local}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void notifyAssignment(String tecnicoEmail, String tecnicoNombre, String subject, String sender) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(tecnicoEmail);
            message.setSubject("Nueva incidencia asignada");
            message.setText("Hola " + tecnicoNombre + ", tienes una incidencia asignada.\n"
                    + "Asunto: " + subject + "\n"
                    + "Remitente: " + sender + "\n");
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("No se pudo enviar email de asignacion a {}: {}", tecnicoEmail, ex.getMessage());
        }
    }
}
