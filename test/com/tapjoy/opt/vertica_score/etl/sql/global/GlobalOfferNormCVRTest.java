package com.tapjoy.opt.vertica_score.etl.sql.global;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.vertica_score.etl.sql.OfferTest;
import com.tapjoy.opt.vertica_score.etl.sql.global.GlobalOfferNormCVR;

public class GlobalOfferNormCVRTest {
	private static Logger logger = Logger.getLogger(OfferTest.class);

	@Test
	public void getOfferSize() {
		Connection conn;
		try {
			conn = VerticaConn.getTestConnection();
			Map<Row, Row> cvrMap = new GlobalOfferNormCVR()
					.getNormCVRMap(conn);

			assertEquals(20569, cvrMap.size());
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
			Map<Row, Row> cvrMap = new GlobalOfferNormCVR()
					.getNormCVRMap(conn);

			for (Row key : cvrMap.keySet()) {
				logger.error(key + "==>" + cvrMap.get(key));
			}
			conn.close();
		} catch (Exception e) {
			logger.error(e);
		}
	}
}
