package com.tapjoy.opt.util;

import java.util.Date;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.tapjoy.opt.util.DateUtil;

public class DateUtilTest {
	private static Logger logger = Logger.getLogger(DateUtilTest.class);

	@Test
	public void getTodayDate() {
		Date today = new Date();
		logger.debug(DateUtil.getTodayDateString());
		
		logger.debug("YYYY:"+DateUtil.getYYYY(today));
		logger.debug("MM:"+DateUtil.getMM(today));
		logger.debug("DD:"+DateUtil.getDD(today));

		logger.debug(DateUtil.getTodayDateString());
		
		int diff = -10;
		logger.debug("YYYY:"+DateUtil.getYYYY(diff));
		logger.debug("MM:"+DateUtil.getMM(diff));
		logger.debug("DD:"+DateUtil.getDD(diff));
		
		logger.debug("DD:"+DateUtil.getDiffDateString(diff));
	}
}
