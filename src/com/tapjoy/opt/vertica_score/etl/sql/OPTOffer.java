package com.tapjoy.opt.vertica_score.etl.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.GenericQuery;
import com.tapjoy.opt.common.NonDeepLinkOfferFilter;
import com.tapjoy.opt.common.NonSelfPromoteOnlyFilter;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.config.OverallConfig;
import com.tapjoy.opt.config.OverallConfig.SQL;
import com.tapjoy.opt.object_cache.OfferCache;
import com.tapjoy.opt.vertica_score.config.Configuration;

public class OPTOffer implements ColumnDef {
	public static String[] columnList = { ID };
	private static Logger logger = Logger.getLogger(OPTOffer.class);

	/**
	 * returning the map for country ID ==> ROW
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static Map<String, Row> getOfferMap(Connection conn, String CVR_SQL, Boolean nonDeeplink,
			Boolean nonSelfPromoteOnly)
			throws SQLException {
		logger.debug("Running sql to get optimized offers::" + CVR_SQL);

		List<Row> offerRank = new GenericQuery().runQuery(conn, CVR_SQL,
				columnList, null, Configuration.SQL.MIN_OPT_OFFER_SIZE,
				SQL.SLEEP_IN_SECONDS, false);
		Map<String, Row> map = new HashMap<String, Row>();
		Map<String, Row> allOfferMap = OfferCache.getInstance().get(); //commented by LJ
		//Map<String, Row> allOfferMap = OfferCache.getInstance().get(GlobalDataManager.conn);  //added by LJ
		
		// filtering out nonDeepLinkOffer
		if (nonDeeplink) {
			allOfferMap = NonDeepLinkOfferFilter.filter(allOfferMap);
		}

		if (nonSelfPromoteOnly) {
			allOfferMap = NonSelfPromoteOnlyFilter.filter(allOfferMap);
		}
		
		for (Row row : offerRank) {
			String offerId = row.getColumn(ID);

			if (allOfferMap.get(offerId) != null) {
				logger.trace("found whole offer record for optimized offer "
						+ offerId);
				map.put(offerId, allOfferMap.get(offerId));
			} else {
				logger.debug("cannot find whole offer record for optimized offer "
						+ offerId);
			}
		}

		if (logger.isDebugEnabled()) {
			for (String offerId : OverallConfig.OfferDebug.tackingOffers) {
				Row row = map.get(offerId);
				if (row != null) {
					logger.debug("Opt offer::" + row);
				} else {
					logger.debug("Opt offer with id not found::" + offerId);
				}
			}
		}

		return map;
	}
}
