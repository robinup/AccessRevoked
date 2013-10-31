/**
 * 
 */
package com.tapjoy.opt.vertica_score.showrate;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.util.DateUtil;
import com.tapjoy.opt.vertica_score.showrate.HourlyConversion;

/**
 * @author lding
 *
 */
public class HourlyConversionTest {
	private static Logger logger = Logger.getLogger( Thread.currentThread().getStackTrace()[0].getClassName());

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Ignore
	public void hourlyConversionTest() {
		Connection conn = null;
		try {
			conn = VerticaConn.getTestConnection();
			Date date1 = new Date();
			
			@SuppressWarnings("deprecation")
			Date date2 = new Date(2013, 1, 20);
			
			int delay = (int) DateUtil.getDateDiffInSeconds(date1, date2);
			Map<Row, Row> map = HourlyConversion.getHourlyConversion(conn, delay);
			assertTrue(map.size() > 0);
		} catch (Exception e) {
			logger.error(e);
			fail("HourlyConversion.getHourlyConversion failed");
		}
	}

}
