package com.tapjoy.opt.vertica_score.etl.sql.global;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.vertica_score.etl.sql.global.GlobalNormCVR;

public class GlobalNormCVRTest {
	private static Logger logger = Logger.getLogger(GlobalNormCVRTest.class);
	
	@Test
	public void getOfferSize() {
		Connection conn;
		try {
			conn = VerticaConn.getTestConnection();
			Map<Row, Row> cvrMap = new GlobalNormCVR()
			.getNormCVRMap(conn);

			assertEquals(58, cvrMap.size());
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
			Map<Row, Row> cvrMap = new GlobalNormCVR()
			.getNormCVRMap(conn);

			for (Row keyRow : cvrMap.keySet()) {
				logger.error(keyRow + "==>" + cvrMap.get(keyRow));
			}
			
			conn.close();
		} catch (Exception e) {
			logger.error(e);
		}
	}
}

