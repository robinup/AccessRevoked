package com.tapjoy.opt.vertica_score.etl.sql.global;

import static org.junit.Assert.assertEquals;
import java.sql.Connection;
import java.util.Map;
import org.apache.log4j.Logger;
import org.junit.Test;

import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.vertica_score.etl.sql.global.GlobalOPTOffer;

public class OptOfferTest {
	private static Logger logger = Logger.getLogger(OptOfferTest.class);

	@Test
	public void getOfferSize() {
		Connection conn;
		try {
			conn = VerticaConn.getTestConnection();
			Map<String, Row> offerIds = GlobalOPTOffer.getOfferMap(conn);

			assertEquals(1141, offerIds.size());
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
			Map<String, Row> offerMap = GlobalOPTOffer.getOfferMap(conn);

			for (Row row : offerMap.values()) {
				logger.debug("offer id::" + row.getColumn("id"));
			}
			
			conn.close();
		} catch (Exception e) {
			logger.error(e);
		}
	}
}

