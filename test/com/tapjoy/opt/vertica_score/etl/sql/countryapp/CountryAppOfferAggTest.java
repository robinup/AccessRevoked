package com.tapjoy.opt.vertica_score.etl.sql.countryapp;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;

import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.vertica_score.etl.sql.countryapp.CountryAppOfferAgg;


public class CountryAppOfferAggTest {
	@Test
	public void updateOfferAggToday() throws SQLException,
			ClassNotFoundException {
		Connection conn = VerticaConn.getTestConnection();
		CountryAppOfferAgg.execute(conn);

		conn.close();
	}
}
