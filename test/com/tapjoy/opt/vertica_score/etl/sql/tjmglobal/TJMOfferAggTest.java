package com.tapjoy.opt.vertica_score.etl.sql.tjmglobal;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.Test;

import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.vertica_score.etl.sql.tjmglobal.TJMOfferAgg;

public class TJMOfferAggTest {
	@Test
	public void updateOfferAggToday() throws SQLException,
			ClassNotFoundException {

		Connection conn = VerticaConn.getTestConnection();
		TJMOfferAgg.execute(conn);

		conn.close();
	}
}
