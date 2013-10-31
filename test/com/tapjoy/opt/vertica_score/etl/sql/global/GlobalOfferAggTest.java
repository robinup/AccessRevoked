package com.tapjoy.opt.vertica_score.etl.sql.global;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Ignore;
import org.junit.Test;
import org.apache.log4j.Logger;

import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.vertica_score.config.Configuration;
import com.tapjoy.opt.vertica_score.etl.sql.global.GlobalOfferAgg;

public class GlobalOfferAggTest {
	Logger logger = Logger.getLogger(GlobalOfferAggTest.class);
	@Ignore
	public void updateGlobalOfferAgg() throws SQLException,
			ClassNotFoundException {
		Connection conn = VerticaConn.getTestConnection();

		GlobalOfferAgg.execute(conn, "2012-11-06", "2012-11-06",
				Configuration.Ranking.MIN_RANK_POSITION);
		conn.close();
	}

	/**
	 * Ignore it for now as it would truncate the data we set up for testing
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void updateGlobalOfferAggToday() throws SQLException,
			ClassNotFoundException {
		Connection conn = VerticaConn.getTestConnection();
		GlobalOfferAgg.execute(conn);
		conn.close();
	}
}
