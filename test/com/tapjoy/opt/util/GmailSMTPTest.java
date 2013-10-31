package com.tapjoy.opt.util;

import javax.mail.MessagingException;
import org.junit.Test;

import com.tapjoy.opt.common.SendEmail;

public class GmailSMTPTest {
	@Test
	public void sendMail() throws MessagingException {
		SendEmail.send("Email Testing", "Testing Send Email");
	}	
}
