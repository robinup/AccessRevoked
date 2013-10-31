package com.tapjoy.opt.vertica_score.etl.sql.tjmcountry;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.GenericQuery;
import com.tapjoy.opt.common.Row;

public class TJMCountryOffer implements ColumnDef {
	public static String CVR_SQL = "select :columnList "
			+ "from offers " +
			"where self_promote_only=0 and item_type!='DeeplinkOffer'";
	
 	public static String[] columnList = { ID, BID, PAYMENT,
			COUNTRIES, DEVICE_TYPES };

	public static String[] keyColumns = { ID };

	/**
	 * returning the map for country ID ==> ROW
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
