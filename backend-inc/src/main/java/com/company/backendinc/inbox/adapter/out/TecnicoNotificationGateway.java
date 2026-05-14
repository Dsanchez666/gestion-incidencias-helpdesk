package com.company.backendinc.inbox.adapter.out;

import com.company.backendinc.mailbox.application.port.out.MailboxConfigPort;
import com.company.backendinc.mailbox.config.SmtpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Properties;

@Component
public class TecnicoNotificationGateway {
    private static final Logger log = LoggerFactory.getLogger(TecnicoNotificationGateway.class);
    private final MailboxConfigPort mailboxConfigPort;

    public TecnicoNotificationGateway(MailboxConfigPort mailboxConfigPort) {
        this.mailboxConfigPort = mailboxConfigPort;
    }

    public void notifyAssignment(String tecnicoEmail, String tecnicoNombre, String subject, String sender,
            String receivedDateTime, String summary, String mailbox, byte[] originalMessageEml) {
        try {
            SmtpConfig smtpConfig = mailboxConfigPort.load().getSmtp();
            if (smtpConfig == null) {
                log.warn("Configuración SMTP no encontrada en Mailboxes_Conf.json");
                return;
            }

            Properties props = createSmtpProperties(smtpConfig);
            Session session = Session.getInstance(props, null);

            MimeMessage message = new MimeMessage(session);
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(smtpConfig.getFrom());
            helper.setTo(tecnicoEmail);
            helper.setSubject("Nueva incidencia asignada - " + subject);
            helper.setText(
                    "Hola " + tecnicoNombre + ",\n\n"
                            + "Se te ha asignado una incidencia nueva.\n\n"
                            + "Datos:\n"
                            + "- Buzon: " + mailbox + "\n"
                            + "- Fecha recepcion: " + receivedDateTime + "\n"
                            + "- Remitente: " + sender + "\n"
                            + "- Asunto: " + subject + "\n"
                            + "- Resumen: " + summary + "\n\n"
                            + "Se adjunta el correo original en formato .eml.\n");
            if (originalMessageEml != null && originalMessageEml.length > 0) {
                helper.addAttachment("correo-original.eml",
                        new org.springframework.core.io.ByteArrayResource(originalMessageEml));
            }

            sendMessage(message, smtpConfig);
            log.info("Email de asignacion enviado a {} exitosamente", tecnicoEmail);
        } catch (Exception ex) {
            log.warn("No se pudo enviar email de asignacion a {}: {}", tecnicoEmail, ex.getMessage());
        }
    }

    public void notifyResolutionToSender(String senderEmail, String subject, String descripcionResolucion, String enlaceSeguimiento) {
        try {
            SmtpConfig smtpConfig = mailboxConfigPort.load().getSmtp();
            if (smtpConfig == null) return;
            Properties props = createSmtpProperties(smtpConfig);
            Session session = Session.getInstance(props, null);
            MimeMessage message = new MimeMessage(session);
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(smtpConfig.getFrom());
            helper.setTo(senderEmail);
            helper.setSubject("Incidencia resuelta - " + subject);
            helper.setText("Tu incidencia se ha marcado como resuelta.\n\nDescripción de resolución:\n"
                    + descripcionResolucion + "\n\nPuedes consultar el detalle aquí:\n" + enlaceSeguimiento);
            sendMessage(message, smtpConfig);
        } catch (Exception ex) {
            log.warn("No se pudo enviar email de resolución a {}: {}", senderEmail, ex.getMessage());
        }
    }

    private Properties createSmtpProperties(SmtpConfig config) {
        Properties props = new Properties();
        props.put("mail.smtp.host", config.getHost());
        props.put("mail.smtp.port", String.valueOf(config.getPort()));
        props.put("mail.smtp.auth", String.valueOf(config.isAuth()));
        props.put("mail.smtp.starttls.enable", String.valueOf(config.isStarttls()));
        props.put("mail.smtp.starttls.required", String.valueOf(config.isStarttls()));
        props.put("mail.smtp.socketFactory.port", String.valueOf(config.getPort()));
        if (config.isStarttls()) {
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }
        return props;
    }

    private void sendMessage(MimeMessage message, SmtpConfig config) throws IOException {
        try {
            if (config.isAuth() && config.getUsername() != null && !config.getUsername().isBlank()) {
                Transport transport = message.getSession().getTransport("smtp");
                transport.connect(config.getHost(), config.getPort(), config.getUsername(), config.getPassword());
                transport.sendMessage(message, message.getAllRecipients());
                transport.close();
            } else {
                Transport.send(message);
            }
        } catch (Exception ex) {
            throw new IOException("Error enviando mensaje SMTP", ex);
        }
    }
}
