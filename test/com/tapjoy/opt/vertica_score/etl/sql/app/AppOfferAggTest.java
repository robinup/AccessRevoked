package com.tapjoy.opt.vertica_score.etl.sql.app;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.Test;

import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.vertica_score.config.Configuration;
import com.tapjoy.opt.vertica_score.etl.sql.app.AppOfferAgg;

public class AppOfferAggTest {
	@Test
	public void updateOfferAgg() throws SQLException,
			ClassNotFoundException {
		Connection conn = VerticaConn.getTestConnection();

		AppOfferAgg.execute(conn, "2012-11-06", "2012-11-06", Configuration.Ranking.MIN_RANK_POSITION, 
				"'8d87c837-0d24-4c46-9d79-46696e042dc5'");
		conn.close();
	}
}
