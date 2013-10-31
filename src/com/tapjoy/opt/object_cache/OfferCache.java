package com.tapjoy.opt.object_cache;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.NonDeepLinkOfferFilter;
import com.tapjoy.opt.common.OfferRowUtil;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.config.OverallConfig.Cache;
import com.tapjoy.opt.sql.Offer;

/**
 * Caching for AllOffer Cache
 * 
 * @author lli
 * 
 * Major Update: Different OfferList Engines will require 
 */
public class OfferCache {
	private static OfferCache instance = new OfferCache();
	private static Logger logger = Logger.getLogger(OfferCache.class);
	
	private static String GENERIC_KEY = "alloffers";

	public static Map<String, CachedEntry> cachedOfferMap = new HashMap<String, CachedEntry>();

	public static OfferCache getInstance() {
		return instance;
	}

	private OfferCache() {
	}
	
	
	public synchronized Map<String, Row> resetMap(Connection conn, boolean featureflag)
			throws SQLException {
		logger.info("OfferCache resetMap");
		
		Map<String, Row> allOfferMap = Offer.getAllOfferMap(conn, featureflag);

		CachedEntry entry = new CachedEntry(GENERIC_KEY, allOfferMap,
				Cache.AllOfferTTL);
		
		cachedOfferMap.put(GENERIC_KEY, entry);
		return allOfferMap;
	}
	
	
	public synchronized void set(Map<String, Row> allOfferMap) {
		CachedEntry entry = new CachedEntry(GENERIC_KEY, allOfferMap,
				Cache.AllOfferTTL);
		
		cachedOfferMap.put(GENERIC_KEY, entry);	
	}

	public synchronized Map<String, Row> get(Connection conn, boolean featureflag) throws SQLException {
		Map<String, Row> om = get();
		if (om == null) {
			om = resetMap(conn, featureflag);
		}
		
		return om;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Row> get() {
		Map<String, Row> allOfferMap = null;
		CachedEntry entry = cachedOfferMap.get(GENERIC_KEY);

		if ((entry != null)) {
			allOfferMap = (Map<String, Row>) entry.getValue();
			return OfferRowUtil.cloneRowMap(allOfferMap);
		}

		return null;
	}
	
	// Return the ref to the original allOfferMap
	// Don't modify this map !!!
	@SuppressWarnings("unchecked")
	public Map<String, Row> getByRef() {
		CachedEntry entry = cachedOfferMap.get(GENERIC_KEY);
		Map<String, Row>  allOfferMap = (Map<String, Row>) entry.getValue();
			
		return allOfferMap;
	}
	
	
	public Map<String, Row> getNonDeepLinkOffer(Connection conn, boolean featureflag)
			throws SQLException {
		Map<String, Row> allRows = get(conn, featureflag);
		Map<String, Row> nonDeepLinkOffer = NonDeepLinkOfferFilter
				.filter(allRows);

		return nonDeepLinkOffer;
	}

	/**
	 * loading the rank boost offer map
	 * 
	 * @param args
	 * @throws SQLException
	 */
	public Map<String, Row> getRankBoostOfferMap(Connection conn, boolean featureflag)
			throws SQLException {
		Map<String, Row> rbOfferMap = new HashMap<String, Row>();

		Map<String, Row> allRows = get(conn, featureflag);
		for (String id : allRows.keySet()) {
			Row row = allRows.get(id).clone();

			if (row.getColumn(ColumnDef.RANK_BOOST).equals("0") == false) {
				rbOfferMap.put(id, row);
			}
		}

		return rbOfferMap;
	}

	/**
	 * loading the rank boost offer map
	 * 
	 * @param args
	 * @throws SQLException
	 */
	public Map<String, Row> getRankBoostOfferMapBySource(Connection conn, 
			String approved_sources, boolean featureflag) throws SQLException {
		Map<String, Row> rankBoostMapBySource = new HashMap<String, Row>();

		Map<String, Row> rankBoostMap = getRankBoostOfferMap(conn, featureflag);
		for (String id : rankBoostMap.keySet()) {
			Row row = rankBoostMap.get(id);
			if (row.getColumn(ColumnDef.APPROVED_SOURCES).toString()
					.contains(approved_sources)) {
				rankBoostMapBySource.put(id, row);
			}
		}

		return rankBoostMapBySource;
	}
}
