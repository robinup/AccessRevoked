package com.tapjoy.opt.vertica_score.etl.sql.country;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Ignore;
import org.junit.Test;

import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.vertica_score.config.Configuration;
import com.tapjoy.opt.vertica_score.etl.sql.country.CountryOfferAgg;

public class CountryOfferAggTest {
	@Ignore
	public void updateOfferAgg() throws SQLException,
	ClassNotFoundException {
		Connection conn = VerticaConn.getTestConnection();

		CountryOfferAgg.executeInit(conn);


		CountryOfferAgg.execute(conn, "2012-11-06", "2019-11-06", Configuration.Ranking.MIN_RANK_POSITION, 
				"'US'");

		CountryOfferAgg.execute(conn, "2012-11-06", "2019-11-06", Configuration.Ranking.MIN_RANK_POSITION, 
				"'CN'");

		CountryOfferAgg.executeCVR(conn);

		conn.close();
	}

	@Ignore
	public void updateOfferAggToday() throws SQLException,
			ClassNotFoundException {
		Connection conn = VerticaConn.getTestConnection();
		CountryOfferAgg.execute(conn);

		conn.close();
	}
}
