package com.tapjoy.opt.common;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import com.tapjoy.opt.config.OverallConfig;

public class SendEmail {
	public static void send(String subject, String text) throws MessagingException {
		// Use Properties object to set environment properties
		Properties props = new Properties();
		props.put("mail.smtp.host", OverallConfig.Email.HOST);
		props.put("mail.smtp.port", OverallConfig.Email.PORT);
		props.put("mail.smtp.user", OverallConfig.Email.USER);
		props.put("mail.smtp.auth", OverallConfig.Email.AUTH);
		props.put("mail.smtp.starttls.enable", OverallConfig.Email.STARTTLS);
		props.put("mail.smtp.debug", OverallConfig.Email.DEBUG);
		props.put("mail.smtp.socketFactory.port", OverallConfig.Email.PORT);
		props.put("mail.smtp.socketFactory.class",
				OverallConfig.Email.SOCKET_FACTORY);
		props.put("mail.smtp.socketFactory.fallback", "false");

		Session session = Session.getDefaultInstance(props, null);
		session.setDebug(true);

		// Construct the mail message
		MimeMessage message = new MimeMessage(session);
		message.setText(text);
		message.setSubject(subject);

		message.setFrom(new InternetAddress(OverallConfig.Email.FROM));
		message.addRecipient(RecipientType.TO, new InternetAddress(
				OverallConfig.Email.TO));
		message.saveChanges();

		// Use Transport to deliver the message
		Transport transport = session.getTransport("smtp");
		transport.connect(OverallConfig.Email.HOST, OverallConfig.Email.USER,
				OverallConfig.Email.PASSWORD);
		transport.sendMessage(message, message.getAllRecipients());

		transport.close();
	}
}