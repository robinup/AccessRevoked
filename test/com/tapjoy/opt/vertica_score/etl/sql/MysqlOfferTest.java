package com.tapjoy.opt.vertica_score.etl.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.vertica_score.etl.sql.MysqlOffer;

@Ignore
public class MysqlOfferTest {
	private static Logger logger = Logger.getLogger(MysqlOfferTest.class);

	@Test
	public void getMysqlOffer() {
		Connection conn;
		try {
			conn = VerticaConn.getMySqlTestConnection();

			Map<String, Row> offerMap = MysqlOffer.getAllOfferMap(conn);

			assertTrue(offerMap.size() > 6500);
			conn.close();
		} catch (Exception e) {
			logger.error("failed in loading", e);
		}
	}

	@Test
	public void getMysqlSingleOffer() {
		Connection conn;
		try {
			conn = VerticaConn.getMySqlTestConnection();

			String offerId = "6adf45e8-0a9b-4293-96e0-b63ce022f0f7";
			Map<String, Row> offerMap = MysqlOffer.getAllOfferMap(conn);

			assertEquals(null, offerMap.get(offerId));
		
			conn.close();
		} catch (Exception e) {
			logger.error("failed in loading", e);
		}
	}

	@Test
	public void getMysqlSingleOffer2() {
		Connection conn;
		try {
			conn = VerticaConn.getMySqlTestConnection();

			String offerId = "6adf45e8-0a9b-4293-96e0-b63ce022f0f7";
			Map<String, Row> offerMap = MysqlOffer.getOfferById(conn, offerId);

			assertEquals(null, offerMap.get(offerId));
		
			conn.close();
		} catch (Exception e) {
			logger.error("failed in loading", e);
		}
	}

}
