package com.volt.comunicacion;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Component
public class MailClient {
	private static final Logger LOG = LoggerFactory.getLogger(MailClient.class);

	private final String host;
	private final int port;
	private final String username;
	private final String password;
	private final String from;
	private final String smtpAuth;

	public MailClient(
			@Value("${mail.smtp.host:localhost}") String host,
			@Value("${mail.smtp.port:587}") int port,
			@Value("${mail.smtp.username:}") String username,
			@Value("${mail.smtp.password:}") String password,
			@Value("${mail.smtp.from:noreply@volt.local}") String from,
			@Value("${mail.smtp.auth:false}") String smtpAuth) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.from = from;
		this.smtpAuth = smtpAuth;
	}

	public void send(String to, String subject, String body) {
		try {
			Properties props = new Properties();
			props.put("mail.smtp.host", host);
			props.put("mail.smtp.port", String.valueOf(port));
			props.put("mail.smtp.auth", smtpAuth);
			props.put("mail.smtp.starttls.enable", "true");
			Session session;
			if ("true".equalsIgnoreCase(smtpAuth)) {
				session = Session.getInstance(props, new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}
				});
			} else {
				session = Session.getInstance(props);
			}
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.setSubject(subject);
			message.setText(body);
			Transport.send(message);
		} catch (Exception exception) {
			LOG.warn("No se pudo enviar email a {}: {}", to, exception.getMessage());
		}
	}
}
