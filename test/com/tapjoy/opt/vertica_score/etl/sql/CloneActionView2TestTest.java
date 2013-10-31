package com.tapjoy.opt.vertica_score.etl.sql;

import java.sql.Connection;

import org.junit.Ignore;
import org.junit.Test;

import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.vertica_score.etl.sql.CloneActionView2Test;

public class CloneActionView2TestTest {
	
	@Ignore
	public void loadingOfferWall() throws Exception {
		Connection conn = null;
		conn = VerticaConn.getTestConnection();

		// whether copying last days' etl data from optimization database to
		// opt_test
		CloneActionView2Test.execute(conn, 2);	
	}
}
