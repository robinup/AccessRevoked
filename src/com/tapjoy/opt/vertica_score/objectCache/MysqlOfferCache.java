package com.tapjoy.opt.vertica_score.objectCache;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.NonDeepLinkOfferFilter;
import com.tapjoy.opt.common.OfferRowUtil;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.config.OverallConfig.Cache;
import com.tapjoy.opt.object_cache.CachedEntry;
import com.tapjoy.opt.vertica_score.etl.sql.MysqlOffer;

/**
 * Caching for All Offer Cache
 * 
 * @author lli
 * 
 */
public class MysqlOfferCache {
	private static MysqlOfferCache instance = new MysqlOfferCache();
	private static Logger logger = Logger.getLogger(MysqlOfferCache.class);

	public static Map<String, CachedEntry> cachedOfferMap = new HashMap<String, CachedEntry>();

	public static MysqlOfferCache getInstance() {
		return instance;
	}

	private MysqlOfferCache() {
	}

	public synchronized Map<String, Row> resetMap() throws SQLException {
		logger.info("resetMap");

		Connection conn = null;
		Map<String, Row> allOfferMap = null;
		try {
			conn = VerticaConn.getMySqlTestConnection();
		} catch (ClassNotFoundException e) {
			logger.error("not found", e);
		} catch (InstantiationException e) {
			logger.error("instantiation failed", e);
		} catch (IllegalAccessException e) {
			logger.error("illegal access", e);
		}

		if (conn == null) {
			return null;
		} else {
			allOfferMap = MysqlOffer.getAllOfferMap(conn);

			CachedEntry entry = new CachedEntry(getNameCacheKey(), allOfferMap,
					Cache.AllOfferTTL);
			cachedOfferMap.put(getNameCacheKey(), entry);

			conn.close();
		}

		return allOfferMap;
	}

	private String getNameCacheKey() {
		String cacheKey = this.getClass() + "::" + "allOfferMap" + "(" + ")";

		return cacheKey;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Row> get() throws SQLException {
		Map<String, Row> allOfferMap = null;

		CachedEntry entry = cachedOfferMap.get(getNameCacheKey());

		if ((entry == null) || entry.isExpired()) {
			logger.debug("cache miss");
			allOfferMap = resetMap();
		} else {
			allOfferMap = (Map<String, Row>) entry.getValue();
		}

		allOfferMap = OfferRowUtil.cloneRowMap(allOfferMap);
		if (logger.isDebugEnabled()) {
			OfferRowUtil.debugOfferRow(allOfferMap.values(),
					"In the All Offer Map");
		}

		return allOfferMap;
	}

	public Map<String, Row> getNonDeepLinkOffer() throws SQLException {
		Map<String, Row> allRows = get();
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
	public Map<String, Row> getRankBoostOfferMap() throws SQLException {
		Map<String, Row> rbOfferMap = new HashMap<String, Row>();

		Map<String, Row> allRows = get();
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
	public Map<String, Row> getRankBoostOfferMapBySource(String approved_sources)
			throws SQLException {
		Map<String, Row> rankBoostMapBySource = new HashMap<String, Row>();

		Map<String, Row> rankBoostMap = getRankBoostOfferMap();
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
