package com.tapjoy.opt.vertica_score.target.app;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.vertica_score.target.app.etl.AppRerunAgg;

public class AppRerunAggTest implements ColumnDef {

	@Test
	public void executeAgg() throws SQLException, ClassNotFoundException {
		Connection conn = VerticaConn.getTestConnection();

		AppRerunAgg.execute(conn);

		conn.close();
	}

}
