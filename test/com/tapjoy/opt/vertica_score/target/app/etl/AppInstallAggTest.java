package com.tapjoy.opt.vertica_score.target.app.etl;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.vertica_score.target.app.etl.AppInstallAgg;

public class AppInstallAggTest {
	Logger logger = Logger.getLogger(AppInstallAggTest.class);
	
	@Test
	public void updateActionAgg() throws SQLException,
			ClassNotFoundException {
		Connection conn = VerticaConn.getTestConnection();

		AppInstallAgg.execute(conn, 2);
		conn.close();
	}

}
