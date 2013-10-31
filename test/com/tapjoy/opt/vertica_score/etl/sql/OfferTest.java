package com.tapjoy.opt.vertica_score.etl.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.*;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.object_cache.OfferCache;

public class OfferTest {
	private static Logger logger = Logger.getLogger(OfferTest.class);

	@Ignore
	public void getOfferSize() {
		Connection conn;
		try {
			conn = VerticaConn.getTestConnection();
			Map<String, Row> offerMap = OfferCache.getInstance().get(conn);

			assertEquals(7832, offerMap.size());
			conn.close();
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Test
	public void getOfferName() {
		Connection conn;
		try {
			conn = VerticaConn.getTestConnection();
			Map<String, Row> offerMap = OfferCache.getInstance().get(conn);

			Row row = offerMap.get("025e4b03-8ef9-437d-9c80-56d2b96bc52b");
			String offerName = row.getColumn(ColumnDef.OFFER_NAME);
			logger.debug("offerName is:"+offerName);
			conn.close();
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	@Test
	public void getOffer() {
		Connection conn;
		try {
			conn = VerticaConn.getTestConnection();
			Map<String, Row> offerMap = OfferCache.getInstance().get(conn);

			for (String key : offerMap.keySet()) {
				logger.debug(key + "==>" + offerMap.get(key));
			}
			conn.close();
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Test
	public void getRankBoostOfferSize() {
		Connection conn;
		try {
			conn = VerticaConn.getTestConnection();
			Map<String, Row> offerMap = OfferCache.getInstance().getRankBoostOfferMap(conn);

			assertEquals(23, offerMap.size());
			conn.close();
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void getRankBoostOffer() throws Exception {
		Connection conn;

		conn = VerticaConn.getTestConnection();
		Map<String, Row> offerMap = OfferCache.getInstance().getRankBoostOfferMap(conn);

		for (String key : offerMap.keySet()) {
			Row row = offerMap.get(key);
			logger.debug(row.getColumn("rank_boost").toString());
			assertTrue(Double.parseDouble(row.getColumn("rank_boost")
					.toString()) != 0.0);
		}
		conn.close();
	}

	@Test
	public void getRankBoostOfferMapBySourceSize() {
		Connection conn;
		try {
			conn = VerticaConn.getTestConnection();
			Map<String, Row> offerMap = OfferCache.getInstance()
					.getRankBoostOfferMapBySource(conn, "offerwall");

			assertEquals(0, offerMap.size());
			conn.close();
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void getRankBoostOfferMapBySource() throws Exception {
		Connection conn;

		conn = VerticaConn.getTestConnection();
		Map<String, Row> offerMap = OfferCache.getInstance().getRankBoostOfferMapBySource(
				conn, "offerwall");

		for (String key : offerMap.keySet()) {
			Row row = offerMap.get(key);
			logger.debug(row.getColumn("rank_boost").toString());
			assertTrue(Double.parseDouble(row.getColumn("rank_boost")
					.toString()) != 0.0);
		}
		conn.close();
	}

	@Test
	public void getNonDeepLinkOfferSize() {
		Connection conn;
		try {
			conn = VerticaConn.getTestConnection();
			Map<String, Row> offerMap = OfferCache.getInstance().getNonDeepLinkOffer(conn);

			assertEquals(2507, offerMap.size());
			conn.close();
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void getNonDeepLinkOffer() throws Exception {
		Connection conn;

		conn = VerticaConn.getTestConnection();
		Map<String, Row> offerMap = OfferCache.getInstance().getNonDeepLinkOffer(conn);

		for (String key : offerMap.keySet()) {
			Row row = offerMap.get(key);
			logger.debug(row.getColumn("item_type").toString());
			assertNotSame("DeeplinkOffer", row.getColumn("item_type"));
		}
		conn.close();
	}
}
