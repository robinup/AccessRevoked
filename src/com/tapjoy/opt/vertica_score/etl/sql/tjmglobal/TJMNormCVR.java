package com.tapjoy.opt.vertica_score.etl.sql.tjmglobal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.GenericQuery;
import com.tapjoy.opt.common.Row;

public class TJMNormCVR implements ColumnDef {
	public static String GLOBAL_CVR_SQL = "select :columnList "
			+ "from tjm_global_norm_ctr_cvr;";
	public static String[] columnList = { OS, OFFERWALL_RANK, CVR };
	public static String[] keyColumns = { OS, OFFERWALL_RANK };
    
	/**
	 * returning the map for global CVR, OS_RANK ==> ROW
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public Map<Row, Row> getNormCVRMap(Connection conn) throws SQLException {
		List<Row> globalRank = new GenericQuery().runQuery(conn,
				GLOBAL_CVR_SQL, columnList, null, false);
		Map<Row, Row> map = Row.toKeyMap(globalRank, keyColumns);
		return map;
	}
}
