package com.tapjoy.opt.vertica_score.etl.sql.tjmcountry;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.Test;

import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.vertica_score.etl.sql.tjmcountry.TJMCountryOfferAgg;

public class TJMCountryOfferAggTest {
	@Test
	public void updateOfferAggToday() throws SQLException,
			ClassNotFoundException {

		Connection conn = VerticaConn.getTestConnection();
		TJMCountryOfferAgg.execute(conn);

		conn.close();
	}
}
