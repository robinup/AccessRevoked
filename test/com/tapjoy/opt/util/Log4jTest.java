package com.tapjoy.opt.util;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Ignore;

public class Log4jTest {
	private static Logger logger = null;

	public static class Bar {
		public void doIt() {
			logger.error("Did it again!");
		}
	}
	
	@Ignore	
	public static void setUp() {
		DOMConfigurator.configure("test/com/tapjoy/opt/common/log4j-test.xml");
		logger = Logger.getLogger(Log4jTest.class);		
	}

	@Ignore	
	public void LogTest() {	
		logger.info("Test Log");
		logger.trace("Entering application.");

		Bar bar = new Bar();
		bar.doIt();

		logger.trace("Exiting application.");
	}
}
