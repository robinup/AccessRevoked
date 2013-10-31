package com.tapjoy.opt.vertica_score.offerGenerator;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.junit.Ignore;

import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.util.DateUtil;
import com.tapjoy.opt.vertica_score.etl.sql.OfferWallActionAgg;
import com.tapjoy.opt.vertica_score.etl.sql.Opt2Test;

public class ETLDataTest {
	private static Logger logger = Logger.getLogger(ETLDataTest.class);

	@Ignore
	public void cloneETLFromProd() throws SQLException, ClassNotFoundException {
		Connection conn = VerticaConn.getTestConnection();

		for (int i = 0; i < 28; i++) {
			Date now = DateUtil.getDiffDate(-i);
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			String theDate = format.format(now);

			// whether copying today's etl data from optimization database to
			// opt_test
			logger.info("executing opt to test clone for the date::" + theDate);
			Opt2Test.execute(conn, theDate);

			// aggregating action table
			OfferWallActionAgg.execute(conn, theDate);
		}
	}
}
