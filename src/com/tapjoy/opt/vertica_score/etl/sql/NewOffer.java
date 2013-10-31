package com.tapjoy.opt.vertica_score.etl.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.NonDeepLinkOfferFilter;
import com.tapjoy.opt.common.NonSelfPromoteOnlyFilter;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.object_cache.OfferCache;

public class NewOffer implements ColumnDef {
	public static String[] columnList = { ID };

	/**
	 * returning the map for country ID ==> ROW
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static Map<String, Row> getOfferMap(Connection conn,
			Map<String, Row> optOfferMap, Boolean nonDeeplink,
			Boolean nonSelfPromoteOnly) throws SQLException {
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

		for (String offerId : allOfferMap.keySet()) {
			if ((optOfferMap.get(offerId) == null)
					&& (allOfferMap.get(offerId) != null)) {
				map.put(offerId, allOfferMap.get(offerId));
			}
		}

		return map;
	}
}
