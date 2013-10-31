/**
 * 
 */
package com.tapjoy.opt.vertica_score.showrate;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.vertica_score.showrate.DailyBudget;

/**
 * @author lding
 *
 */
public class DailyBudgetTest {	
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
	public void dailyBudgetTest() {
        Connection conn = null;
        try {
            conn = VerticaConn.getTestConnection();

            Map<Row, Row> map = DailyBudget.getDailyBudget(conn);     
            assertTrue(map.size()>1000);
        } catch (Exception e) {
            logger.error(e);
            fail("DailyBudget.getDailyBudget failed");
        }
	}

}
