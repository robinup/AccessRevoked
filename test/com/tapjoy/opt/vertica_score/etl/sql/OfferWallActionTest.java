package com.tapjoy.opt.vertica_score.etl.sql;

import java.sql.Connection;
import org.apache.log4j.Logger;

import org.junit.Ignore;

import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.util.DateUtil;
import com.tapjoy.opt.vertica_score.etl.sql.OfferWallAction;

public class OfferWallActionTest {
	private static Logger logger = Logger.getLogger(OfferWallActionTest.class);

	@Ignore
	public void updateOfferWallAction() {
		Connection conn = null;
		try {
			conn = VerticaConn.getTestConnection();
			String startDate = DateUtil.getTodayDateString();
			logger.error("Today's date is " + startDate);

			OfferWallAction.execute(conn, "2012-11-06");
			conn.close();
		} catch (Exception e) {
			logger.error(e);
		}
	}
}
