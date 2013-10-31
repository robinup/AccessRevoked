package com.tapjoy.opt.vertica_score.etl.sql.country;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.vertica_score.etl.sql.OPTOffer;

public class CountryOPTOffer implements ColumnDef {
	public static String CVR_SQL = "select distinct offer_id as id "
			+ "from gen_country_offer_ctr_cvr";
	public static String[] columnList = { ID };

	/**
	 * returning the map for country ID ==> ROW
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static Map<String, Row> getOfferMap(Connection conn)
			throws SQLException {
		return OPTOffer.getOfferMap(conn, CVR_SQL, true, false);
	}
}
