package com.company.backendinc.inbox.adapter.out;

import com.company.backendinc.mailbox.application.port.out.MailboxConfigPort;
import com.company.backendinc.mailbox.config.SmtpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Properties;

@Component
public class TecnicoNotificationGateway {
    private static final Logger log = LoggerFactory.getLogger(TecnicoNotificationGateway.class);
    private static final String DEFAULT_FROM = "ServicioNotificacionETNA@enaire.es";
    private final MailboxConfigPort mailboxConfigPort;

    public TecnicoNotificationGateway(MailboxConfigPort mailboxConfigPort) {
        this.mailboxConfigPort = mailboxConfigPort;
    }

    public void sendEmail(EmailRequest request) {
        try {
            SmtpConfig smtpConfig = mailboxConfigPort.load().getSmtp();
            if (smtpConfig == null) {
                log.warn("Configuración SMTP no encontrada en Mailboxes_Conf.json");
                return;
            }
            if (smtpConfig.getHost() == null || smtpConfig.getHost().isBlank() || smtpConfig.getPort() <= 0) {
                log.warn("Configuración SMTP inválida: host='{}', port={}", smtpConfig.getHost(), smtpConfig.getPort());
                return;
            }

            Properties props = createSmtpProperties(smtpConfig);
            Session session = Session.getInstance(props, null);

            MimeMessage message = new MimeMessage(session);
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            String from = (smtpConfig.getFrom() == null || smtpConfig.getFrom().isBlank()) ? DEFAULT_FROM : smtpConfig.getFrom();
            log.info("SMTP send attempt -> from='{}', to='{}', subject='{}', host='{}', port={}",
                    from, request.to(), request.subject(), smtpConfig.getHost(), smtpConfig.getPort());
            helper.setFrom(from);
            helper.setTo(request.to());
            helper.setSubject(request.subject());
            helper.setText(request.bodyText());
            if (request.attachmentContent() != null && request.attachmentContent().length > 0
                    && request.attachmentName() != null && !request.attachmentName().isBlank()) {
                helper.addAttachment(request.attachmentName(),
                        new org.springframework.core.io.ByteArrayResource(request.attachmentContent()));
            }

            sendMessage(message);
            log.info("Email '{}' enviado a {} exitosamente", request.subject(), request.to());
        } catch (Exception ex) {
            Throwable root = rootCause(ex);
            log.warn("No se pudo enviar email a {}: {} (causa: {})",
                    request.to(),
                    ex.getMessage(),
                    root == null ? "desconocida" : root.getMessage());
        }
    }

    private Properties createSmtpProperties(SmtpConfig config) {
        Properties props = new Properties();
        props.put("mail.smtp.host", config.getHost());
        props.put("mail.smtp.port", String.valueOf(config.getPort()));
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        return props;
    }

    private void sendMessage(MimeMessage message) throws IOException {
        try {
            Transport.send(message);
        } catch (Exception ex) {
            throw new IOException("Error enviando mensaje SMTP", ex);
        }
    }

    public record EmailRequest(
            String to,
            String subject,
            String bodyText,
            String attachmentName,
            byte[] attachmentContent) {
    }

    private Throwable rootCause(Throwable ex) {
        Throwable current = ex;
        while (current != null && current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
