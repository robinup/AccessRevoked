package com.tapjoy.opt.common;

import java.util.ArrayList;
import java.util.List;
import javax.mail.MessagingException;

import org.apache.log4j.Logger;

import com.tapjoy.opt.config.OverallConfig;
import com.tapjoy.opt.util.DateUtil;

public class EmailLogger {
	private List<String> infoMessageList = new ArrayList<String>();
	private List<String> errorMessageList = new ArrayList<String>();
	private String subject;
	private static EmailLogger instance = new EmailLogger();

	private EmailLogger() {
	}

	public static EmailLogger getInstance() {
		return instance;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void debug(Logger logger, String message) {
		logger.debug(message);
		infoMessageList.add("[DEBUG] " + DateUtil.getCurrentTimeStampString()
				+ " " + message);
	}

	public void info(Logger logger, String message) {
		logger.info(message);
		infoMessageList.add("[INFO] " + DateUtil.getCurrentTimeStampString()
				+ " " + message);
	}

	public void error(Logger logger, String message) {
		logger.error(message);
		errorMessageList.add("[ERROR] " + DateUtil.getCurrentTimeStampString()
				+ " " + message);
	}

	public void error(Logger logger, String message, Exception e) {
		logger.error(message, e);
		errorMessageList.add("[ERROR] " + DateUtil.getCurrentTimeStampString()
				+ " " + message + " Exception:" + e.getMessage());
	}

	public void fatal(Logger logger, String message) {
		logger.fatal(message);
		errorMessageList.add("[FATAL] " + DateUtil.getCurrentTimeStampString()
				+ " " + message);
	}

	public void fatal(Logger logger, String message, Exception e) {
		logger.fatal(message, e);
		errorMessageList.add("[FATAL] " + DateUtil.getCurrentTimeStampString()
				+ " " + message + " Exception:" + e.getMessage());
	}

	public void fatal(Logger logger, String message, Throwable t) {
		logger.fatal(message, t);
		errorMessageList.add("[FATAL] " + DateUtil.getCurrentTimeStampString()
				+ " " + message + " Exception:" + t.getMessage());
	}

	public void flush() throws MessagingException {
		StringBuffer buff = new StringBuffer();

		for (String message : infoMessageList) {
			buff.append(message + "\n");
		}

		for (String message : errorMessageList) {
			buff.append(message + "\n");
		}

		if (subject == null || subject.length() == 0) {
			subject = OverallConfig.Email.SUBJECT;
		}
		
		String content = buff.toString();
		if (content.length() < 10) {
          // Not enough info for an email
          return;
        } else {
			SendEmail.send(subject, buff.toString());
		}
		

		infoMessageList.clear();
		errorMessageList.clear();
		subject = "";
	}
}
