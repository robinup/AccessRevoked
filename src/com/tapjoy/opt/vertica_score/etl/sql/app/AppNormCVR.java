package com.tapjoy.opt.vertica_score.etl.sql.app;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.GenericQuery;
import com.tapjoy.opt.common.Row;

public class AppNormCVR implements ColumnDef {
	public static String CVR_SQL = "select :columnList "
			+ "from gen_app_norm_ctr_cvr;";
	
	public static String[] columnList = { OS, CURRENCY_ID, OFFERWALL_RANK, CVR };
	public static String[] keyColumns = { OS, CURRENCY_ID, OFFERWALL_RANK };

	/**
	 * returning the map for global CVR, OS_RANK ==> ROW
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public Map<Row, Row> getNormCVRMap(Connection conn) throws SQLException {
		List<Row> offerRank = new GenericQuery().runQuery(conn,
				CVR_SQL, columnList, null, false);
		Map<Row, Row> map = Row.toKeyMap(offerRank, keyColumns);
		return map;
	}
}
