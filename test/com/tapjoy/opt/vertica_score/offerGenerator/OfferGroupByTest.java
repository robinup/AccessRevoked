package com.tapjoy.opt.vertica_score.offerGenerator;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.junit.Test;

import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.vertica_score.etl.sql.app.AppOfferNormCVR;
import com.tapjoy.opt.vertica_score.offerGenerator.OfferRankGroupBy;

public class OfferGroupByTest {

	@Test
	public void groupBy() throws SQLException, ClassNotFoundException {
		Connection conn = VerticaConn.getTestConnection();
		
		// OS, CURRENCY_ID?, OFFER_ID, OFFERWALL_RANK ==> ROW
		Map<Row, Row> offerRankCVRMap = new AppOfferNormCVR().getNormCVRMap(conn);

		// { OS, CURRENCY_ID?, OFFER_ID, OFFERWALL_RANK ==> ROW ==> VIEWS_SUM, ACTIONS_SUM }
		Map<Row, Row> groupOfferByMap = new OfferRankGroupBy()
				.groupBy(offerRankCVRMap);
		
		assertEquals(6493, groupOfferByMap.size());
	}
}
