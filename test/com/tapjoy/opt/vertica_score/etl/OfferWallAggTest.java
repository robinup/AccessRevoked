package com.tapjoy.opt.vertica_score.etl;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.vertica_score.etl.sql.OfferWallAction;
import com.tapjoy.opt.vertica_score.etl.sql.OfferWallActionAgg;
import com.tapjoy.opt.vertica_score.etl.sql.OfferWallViewAgg;
import com.tapjoy.opt.vertica_score.etl.sql.app.AppOfferAgg;
import com.tapjoy.opt.vertica_score.etl.sql.country.CountryOfferAgg;
import com.tapjoy.opt.vertica_score.etl.sql.countryapp.CountryAppOfferAgg;
import com.tapjoy.opt.vertica_score.etl.sql.global.GlobalOfferAgg;
import com.tapjoy.opt.vertica_score.etl.sql.tjmcountry.TJMCountryOfferAgg;
import com.tapjoy.opt.vertica_score.etl.sql.tjmglobal.TJMOfferAgg;

public class OfferWallAggTest {
	private static Logger logger = Logger.getLogger(OfferWallAggTest.class);
	
	public void updateOfferWallAction() {
		Connection conn = null;
		try {
			conn = VerticaConn.getTestConnection();
			
			OfferWallAction.execute(conn, "2013-02-03");
			OfferWallAction.execute(conn, "2013-02-04");
			OfferWallAction.execute(conn, "2013-02-05");

			conn.close();
		} catch (Exception e) {
			logger.error(e);
		}
	}

	public void updateOfferWallActionAgg() {
		Connection conn = null;
		try {
			conn = VerticaConn.getTestConnection();
	
			OfferWallActionAgg.execute(conn, "2013-02-03");
			OfferWallActionAgg.execute(conn, "2013-02-04");
			OfferWallActionAgg.execute(conn, "2013-02-05");

			conn.close();
		} catch (Exception e) {
			logger.error(e);
		}
	}

	public void updateOfferWallViewAgg() {
		Connection conn = null;
		try {
			conn = VerticaConn.getTestConnection();

			OfferWallViewAgg.execute(conn, "2013-02-03");
			OfferWallViewAgg.execute(conn, "2013-02-04");
			conn.close();
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Ignore
	public void updateOfferAgg() throws SQLException,
			ClassNotFoundException {
		Connection conn = VerticaConn.getTestConnection();
		
		updateOfferWallAction();
		updateOfferWallActionAgg();
		updateOfferWallViewAgg();
		
		GlobalOfferAgg.execute(conn);
		CountryOfferAgg.execute(conn);
		AppOfferAgg.execute(conn);
		CountryAppOfferAgg.execute(conn);
		TJMOfferAgg.execute(conn);
		TJMCountryOfferAgg.execute(conn);
		
		conn.close();
	}

}
