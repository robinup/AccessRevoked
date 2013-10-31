package com.tapjoy.opt.vertica_score.etl.sql.tjmcountry;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.vertica_score.etl.sql.NewOffer;

public class TJMCountryNewOffer implements ColumnDef {
	/**
	 * returning the map for country ID ==> ROW
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static Map<String, Row> getOfferMap(Connection conn)
			throws SQLException {
		Map<String, Row> optOfferMap = TJMCountryOPTOffer.getOfferMap(conn);
		Map<String, Row> map = NewOffer.getOfferMap(conn, optOfferMap, true, true);

		return map;
	}
}
