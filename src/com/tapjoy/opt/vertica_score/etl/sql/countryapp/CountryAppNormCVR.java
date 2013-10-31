package com.tapjoy.opt.vertica_score.etl.sql.countryapp;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.GenericQuery;
import com.tapjoy.opt.common.Row;

public class CountryAppNormCVR implements ColumnDef {
	public static String COUNTRY_CVR_SQL = "select :columnList "
			+ "from gen_country_app_norm_ctr_cvr";
	
	public static String[] columnList = { OS, CURRENCY_ID, COUNTRY, OFFERWALL_RANK, CVR };
	public static String[] keyColumns = { OS, CURRENCY_ID, COUNTRY, OFFERWALL_RANK };

	/**
	 * returning the map for global CVR, OS_RANK ==> ROW
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public Map<Row, Row> getNormCVRMap(Connection conn) throws SQLException {
		List<Row> countryRank = new GenericQuery().runQuery(conn,
				COUNTRY_CVR_SQL, columnList, null, false);
		Map<Row, Row> map = Row.toKeyMap(countryRank, keyColumns);
		return map;
	}
}
