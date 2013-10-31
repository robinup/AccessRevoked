package com.tapjoy.opt.vertica_score.etl.sql.global;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.vertica_score.etl.sql.OPTOffer;

public class GlobalOPTOffer implements ColumnDef {
	private static Logger logger = Logger.getLogger(GlobalOPTOffer.class);

	public static String CVR_SQL = "select distinct offer_id as id "
			+ "from gen_global_offer_ctr_cvr";

	/**
	 * returning the map for country ID ==> ROW
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static Map<String, Row> getOfferMap(Connection conn)
			throws SQLException {
		// scan through all the live offers, exclude free-promo offers, if
		// offers have enough data to optimize, produce a rank score, if not,
		// then put into the new offer pool
		Map<String, Row> offerMap = OPTOffer.getOfferMap(conn, CVR_SQL, true, false);
		// offerMap = NonDeepLinkOfferFilter.filter(offerMap);

		logger.info("Total global In-App-Offerwall offers : " + offerMap.size());

		return offerMap;
	}
}
