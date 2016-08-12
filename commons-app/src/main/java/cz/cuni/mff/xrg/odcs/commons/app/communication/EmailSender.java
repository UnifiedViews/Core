package cz.cuni.mff.xrg.odcs.commons.app.communication;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.conf.MissingConfigPropertyException;

/**
 * Enable clients to send email. The functionality can be set that the {@link EmailSender} silently ignore send request.
 * 
 * @author Petyr
 */
public class EmailSender {

    private static final Logger LOG = LoggerFactory.getLogger(EmailSender.class);

    /**
     * If false outcoming email are silently dropped.
     */
    private final boolean enabled;

    /**
     * Address of SMTP server.
     */
    private String smtpHost;

    /**
     * Port used by SMTP server.
     */
    private String smtpPort;

    /**
     * True if use TTL.
     */
    private boolean useTLS;

    /**
     * From email address.
     */
    private String fromEmail;

    /**
     * From displayed name
     */
    private String fromName;

    /**
     * Use authentication?
     */
    private Boolean authentication;

    /**
     * User name for authentication.
     */
    private String username;

    /**
     * User password for authentication.
     */
    private String password;

    /**
     * Create email sender based on application configuration.
     * 
     * @param appConfig
     *            Application configuration.
     */
    public EmailSender(AppConfig appConfig) {

        this.enabled = appConfig.getBoolean(ConfigProperty.EMAIL_ENABLED);

        if (this.enabled) {
            this.smtpHost = appConfig.getString(ConfigProperty.EMAIL_SMTP_HOST);
            this.smtpPort = appConfig.getString(ConfigProperty.EMAIL_SMTP_PORT);
            this.useTLS = appConfig.getBoolean(ConfigProperty.EMAIL_SMTP_TLS);
            this.fromEmail = appConfig.getString(ConfigProperty.EMAIL_FROM_EMAIL);
            this.authentication = appConfig.getBoolean(ConfigProperty.EMAIL_AUTHORIZATION);

            try {
                this.fromName = appConfig.getString(ConfigProperty.EMAIL_FROM_NAME);
            } catch (MissingConfigPropertyException ignore) {
                // optional parameter - backward compatible
            }

            if (this.authentication) {
                // get data for authentication
                this.username = appConfig.getString(ConfigProperty.EMAIL_USERNAME);
                this.password = appConfig.getString(ConfigProperty.EMAIL_PASSWORD);
            }
        }
    }

    /**
     * Send email with HTML content. If the list of recipients
     * is empty then immediately return false.
     * 
     * @param subject
     *            Email subject.
     * @param body
     *            Email content.
     * @param recipients
     *            List of recipients.
     * @return True if and only if email has been send.
     */
    public boolean send(String subject, String body, List<String> recipients) {
        LOG.debug("send({}, , size: {})", subject, recipients.size());

        if (!enabled || recipients.isEmpty()) {
            LOG.debug("Email send request has been ignored ... ");
            return false;
        }

        // prepare properties
        Properties props = new Properties();
        if (useTLS) {
            props.put("mail.smtp.starttls.enable", "true");
        }
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        // create session based on authentication

        props.put("mail.pop3s.ssl.trust", "*");

        Session session;
        if (authentication) {
            props.put("mail.smtp.auth", "true");
            session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username,
                                    password);
                        }
                    });
        } else {
            props.put("mail.smtp.auth", "false");
            session = Session.getDefaultInstance(props, null);
        }

        // create message
        String encodingOptions = "text/html; charset=UTF-8";
        Message msg = new MimeMessage(session);
        try {
            msg.setHeader("Content-Type", encodingOptions);
            msg.setFrom(new InternetAddress(this.fromEmail, this.fromName));
            for (String email : recipients) {
                msg.addRecipient(Message.RecipientType.TO, new InternetAddress(
                        email));
            }

            msg.setSubject(subject);
            msg.setContent(body, encodingOptions);
            // send message
            Transport.send(msg);
        } catch (MessagingException | UnsupportedEncodingException e) {
            LOG.error("Failed to send email.", e);
            return false;
        }

        LOG.debug("Email has been send");
        return true;
    }

    /**
     * Sends email to a single recipient.
     * 
     * @param subject
     *            Email subject.
     * @param body
     *            Email content.
     * @param recipient
     *            Email address of a single recipient.
     * @return True if and only if email has been send.
     */
    public boolean send(String subject, String body, String recipient) {
        return send(subject, body, Arrays.asList(recipient));
    }

}
