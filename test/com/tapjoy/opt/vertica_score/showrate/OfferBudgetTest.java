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
import com.tapjoy.opt.vertica_score.showrate.OfferBudget;

public class OfferBudgetTest {
	private static Logger logger = Logger.getLogger( Thread.currentThread().getStackTrace()[0].getClassName());
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Ignore
	public void budgetTest() {
		Connection conn = null;
		try {
			conn = VerticaConn.getTestConnection();
			OfferBudget ob = new OfferBudget();

			Map<String, Double> map = ob.retrieveShowRate(conn);
			assertTrue(map.size() > 0);
		} catch (Exception e) {
			logger.error(e);
			fail("DailyBudget.getDailyBudget failed");
		}
		
	}

}
