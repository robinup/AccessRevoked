package com.tapjoy.opt.vertica_score.etl.sql.global;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.GenericQuery;
import com.tapjoy.opt.common.OfferRowUtil;
import com.tapjoy.opt.common.Row;

public class GlobalOfferNormCVR implements ColumnDef {
	private static Logger logger = Logger.getLogger(GlobalOfferNormCVR.class);

	public static String GLOBAL_Offer_CVR_SQL = "select :columnList "
			+ "from gen_global_offer_ctr_cvr " + "order by 1,2,3";
	public static String[] columnList = { OS, OFFER_ID, OFFERWALL_RANK,
			IMPRESSIONS, CLICKS, ACTIONS };
	public static String[] keyColumns = { OS, OFFER_ID, OFFERWALL_RANK };

	/**
	 * returning the map for global CVR, OS_OFFERID_RANK ==> ROW
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public Map<Row, Row> getNormCVRMap(Connection conn) throws SQLException {
		List<Row> globalOfferRank = new GenericQuery().runQuery(conn,
				GLOBAL_Offer_CVR_SQL, columnList, null, false);
		Map<Row, Row> map = Row.toKeyMap(globalOfferRank, keyColumns);

		if(logger.isDebugEnabled()) {
			OfferRowUtil.debugGroupByMap(map, "getting Normal CVR map");
		}
		
		return map;
	}
}
