package com.tapjoy.opt.vertica_score.etl;

import java.sql.Connection;
import java.sql.SQLException;

import com.tapjoy.opt.vertica_score.etl.sql.OfferWallActionAgg;
import com.tapjoy.opt.vertica_score.etl.sql.OfferWallViewAgg;

public class OfferWallAgg {
	public static void execute(Connection conn, String startDate) throws SQLException {
		OfferWallViewAgg.execute(conn, startDate);
		OfferWallActionAgg.execute(conn, startDate);
	}
}
