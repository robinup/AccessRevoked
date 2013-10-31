package com.tapjoy.opt.util;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.tapjoy.opt.common.EmailLogger;

public class EmailLoggerTest {
	private static Logger logger = Logger.getLogger(EmailLoggerTest.class);

	@Test
	public void loggerTest() throws MessagingException {
		EmailLogger emailLogger =  EmailLogger.getInstance();
		
		emailLogger.debug(logger, "Debug 1");
		emailLogger.debug(logger, "Debug 2");

		emailLogger.info(logger, "Info 1");
		emailLogger.info(logger, "Info 2");
		
		emailLogger.error(logger, "Error 1");
		emailLogger.error(logger, "Error 2");

		emailLogger.fatal(logger, "Fatal 1");
		emailLogger.fatal(logger, "Fatal 2");
		
		emailLogger.setSubject("Optimization Stats Update");
		
		emailLogger.flush();
	}
}
