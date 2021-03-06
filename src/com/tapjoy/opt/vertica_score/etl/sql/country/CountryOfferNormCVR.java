package com.tapjoy.opt.vertica_score.etl.sql.country;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.GenericQuery;
import com.tapjoy.opt.common.Row;

public class CountryOfferNormCVR implements ColumnDef {
	public static String COUNTRY_Offer_CVR_SQL = "select :columnList "
			+ "from gen_country_offer_ctr_cvr " +
			"order by 1,2,3,4";
	
	public static String[] columnList = {OS, COUNTRY, OFFER_ID, OFFERWALL_RANK,
		IMPRESSIONS, CLICKS, ACTIONS };

	public static String[] keyColumns = { OS, COUNTRY, OFFER_ID, OFFERWALL_RANK };

	/**
	 * returning the map for country ID ==> ROW
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public Map<Row, Row> getNormCVRMap(Connection conn) throws SQLException {
		List<Row> countryOfferRank = new GenericQuery().runQuery(conn,
				COUNTRY_Offer_CVR_SQL, columnList, null, false);
		Map<Row, Row> map = Row.toKeyMap(countryOfferRank, keyColumns);
		
		return map;
	}
}
