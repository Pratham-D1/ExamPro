package com.exampro.util;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * EmailService – Asynchronous, resilient email dispatch service with configurable SMTP
 * via db.properties or environment variables. Operates strictly non-blocking.
 */
public class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "exampro-email-worker");
        t.setDaemon(true);
        return t;
    });

    private static final String PROP_FILE = "/db.properties";

    private static String smtpHost;
    private static int smtpPort = 587;
    private static String smtpUser;
    private static String smtpPass;
    private static String fromAddress = "no-reply@exampro.com";
    private static boolean smtpAuth = false;
    private static boolean starttls = true;
    private static boolean emailEnabled = false;

    static {
        loadConfiguration();
    }

    private static synchronized void loadConfiguration() {
        Properties props = new Properties();
        try (InputStream in = EmailService.class.getResourceAsStream(PROP_FILE)) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception ignored) {}

        // Environment variables override properties file
        smtpHost = getVal(System.getenv("EXAMPRO_SMTP_HOST"), props.getProperty("mail.smtp.host"));
        String portStr = getVal(System.getenv("EXAMPRO_SMTP_PORT"), props.getProperty("mail.smtp.port"));
        if (portStr != null && !portStr.isBlank()) {
            try {
                smtpPort = Integer.parseInt(portStr.trim());
            } catch (NumberFormatException ignored) {}
        }

        smtpUser = getVal(System.getenv("EXAMPRO_SMTP_USER"), props.getProperty("mail.smtp.username"));
        smtpPass = getVal(System.getenv("EXAMPRO_SMTP_PASS"), props.getProperty("mail.smtp.password"));
        fromAddress = getVal(System.getenv("EXAMPRO_SMTP_FROM"), props.getProperty("mail.smtp.from"));
        if (fromAddress == null || fromAddress.isBlank()) {
            fromAddress = "no-reply@exampro.com";
        }

        String authStr = getVal(System.getenv("EXAMPRO_SMTP_AUTH"), props.getProperty("mail.smtp.auth"));
        smtpAuth = (authStr != null) ? Boolean.parseBoolean(authStr.trim()) : (smtpUser != null && !smtpUser.isBlank());

        String tlsStr = getVal(System.getenv("EXAMPRO_SMTP_STARTTLS"), props.getProperty("mail.smtp.starttls.enable"));
        starttls = (tlsStr != null) ? Boolean.parseBoolean(tlsStr.trim()) : true;

        emailEnabled = (smtpHost != null && !smtpHost.isBlank());
        if (emailEnabled) {
            LOGGER.info("EmailService: SMTP initialized (" + smtpHost + ":" + smtpPort + "). Outbound emails active.");
        } else {
            LOGGER.info("EmailService: SMTP host unconfigured. Outbound emails safely disabled (in-app notifications active).");
        }
    }

    private static String getVal(String env, String prop) {
        if (env != null && !env.isBlank()) return env.trim();
        if (prop != null && !prop.isBlank()) return prop.trim();
        return null;
    }

    public static boolean isEmailEnabled() {
        return emailEnabled;
    }

    /**
     * Non-blocking asynchronous email delivery. Never fails caller or interrupts database transactions.
     */
    public static void sendEmailAsync(String to, String subject, String htmlBody) {
        if (!emailEnabled || to == null || to.isBlank()) {
            LOGGER.fine("Email sending skipped for " + to + " (Service not configured or recipient empty).");
            return;
        }

        EXECUTOR.submit(() -> {
            try {
                sendEmailDirect(to.trim(), subject, htmlBody);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to deliver email to [" + to + "]: " + e.getMessage(), e);
            }
        });
    }

    public static void sendEmailDirect(String to, String subject, String htmlBody) throws MessagingException {
        if (!emailEnabled) {
            return;
        }

        Properties mailProps = new Properties();
        mailProps.put("mail.smtp.host", smtpHost);
        mailProps.put("mail.smtp.port", String.valueOf(smtpPort));
        mailProps.put("mail.smtp.auth", String.valueOf(smtpAuth));
        mailProps.put("mail.smtp.starttls.enable", String.valueOf(starttls));
        mailProps.put("mail.smtp.connectiontimeout", "5000");
        mailProps.put("mail.smtp.timeout", "5000");

        Session session;
        if (smtpAuth && smtpUser != null && smtpPass != null) {
            session = Session.getInstance(mailProps, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUser, smtpPass);
                }
            });
        } else {
            session = Session.getInstance(mailProps);
        }

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(fromAddress));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        msg.setSubject("[ExamPro] " + subject, "UTF-8");
        msg.setContent(wrapTemplate(subject, htmlBody), "text/html; charset=UTF-8");

        Transport.send(msg);
        LOGGER.info("Email delivered to " + to + " - " + subject);
    }

    private static String wrapTemplate(String header, String content) {
        return "<!DOCTYPE html><html><body style='font-family: Arial, sans-serif; background-color: #0d1117; color: #c9d1d9; padding: 24px;'>" +
               "<div style='max-width: 600px; margin: 0 auto; background-color: #161b22; border: 1px solid #30363d; border-radius: 8px; overflow: hidden;'>" +
               "<div style='background-color: #1f6feb; color: #ffffff; padding: 16px 24px; font-weight: bold; font-size: 18px;'>ExamPro &bull; Online Examination System</div>" +
               "<div style='padding: 24px;'>" +
               "<h2 style='color: #ffffff; margin-top: 0; font-size: 18px;'>" + header + "</h2>" +
               "<div style='color: #8b949e; line-height: 1.6; font-size: 14px;'>" + content + "</div>" +
               "</div>" +
               "<div style='background-color: #0d1117; padding: 12px 24px; font-size: 12px; color: #6e7681; border-top: 1px solid #21262d;'>" +
               "This is an automated notification from ExamPro. Please do not reply directly to this email." +
               "</div></div></body></html>";
    }
}

