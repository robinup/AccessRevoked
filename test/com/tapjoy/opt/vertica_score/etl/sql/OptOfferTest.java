package com.tapjoy.opt.vertica_score.etl.sql;

import java.sql.Connection;

import org.junit.Ignore;
import org.junit.Test;

import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.vertica_score.etl.sql.Opt2Test;

public class OptOfferTest {

	@Ignore
	public void loadingOfferWall() throws Exception {
		Connection conn = null;
		conn = VerticaConn.getTestConnection();

		// whether copying last 10 days' etl data from optimization database to
		// opt_test
		Opt2Test.execute(conn, 10);	
	}
}
