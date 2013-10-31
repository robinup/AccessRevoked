package com.tapjoy.opt.vertica_score.etl.sql.tjmcountry;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.GenericQuery;
import com.tapjoy.opt.common.Row;

public class TJMCountryNormCVR implements ColumnDef {
	public static String CVR_SQL = "select :columnList "
			+ "from tjm_country_norm_ctr_cvr;";
	public static String[] columnList = { OS, COUNTRY, OFFERWALL_RANK, CVR };
	public static String[] keyColumns = { OS, COUNTRY, OFFERWALL_RANK };
    
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
