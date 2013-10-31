package com.tapjoy.opt.vertica_score.objectCache;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.object_cache.CachedEntry;
import com.tapjoy.opt.vertica_score.config.Configuration;
import com.tapjoy.opt.vertica_score.config.ConfigurationSegment;
import com.tapjoy.opt.vertica_score.etl.sql.app.AppNewOffer;
import com.tapjoy.opt.vertica_score.etl.sql.country.CountryNewOffer;
import com.tapjoy.opt.vertica_score.etl.sql.countryapp.CountryAppNewOffer;
import com.tapjoy.opt.vertica_score.etl.sql.global.GlobalNewOffer;
import com.tapjoy.opt.vertica_score.etl.sql.tjmcountry.TJMCountryNewOffer;
import com.tapjoy.opt.vertica_score.etl.sql.tjmglobal.TJMNewOffer;

/**
 * Caching for All Offer Cache
 * 
 * @author lli
 * 
 */
public class NewOfferCache {
	private static NewOfferCache instance = new NewOfferCache();
	private static Logger logger = Logger.getLogger(NewOfferCache.class);

	public static Map<String, CachedEntry> cachedOfferMap = new HashMap<String, CachedEntry>();

	public static NewOfferCache getInstance() {
		return instance;
	}

	private NewOfferCache() {
	}

	public synchronized Map<String, Row> resetMap(Connection conn,
			String segment) throws SQLException {
		logger.debug("resetMap<");

		Map<String, Row> newOfferMap = new HashMap<String, Row>();

		if (segment.equals(ConfigurationSegment.OPT_GOW_GLOBAL)) {
			newOfferMap = GlobalNewOffer.getOfferMap(conn);
		} else if (segment.equals(ConfigurationSegment.OPT_GOW_COUNTRY)) {
			newOfferMap = CountryNewOffer.getOfferMap(conn);
		} else if (segment.equals(ConfigurationSegment.OPT_GOW_APP)) {
			newOfferMap = AppNewOffer.getOfferMap(conn);
		} else if (segment.equals(ConfigurationSegment.OPT_GOW_COUNTRY_APP)) {
			newOfferMap = CountryAppNewOffer.getOfferMap(conn);
		} else if (segment.equals(ConfigurationSegment.OPT_TJM)) {
			newOfferMap = TJMNewOffer.getOfferMap(conn);
		} else if (segment.equals(ConfigurationSegment.OPT_TJM_COUNTRY)) {
			newOfferMap = TJMCountryNewOffer.getOfferMap(conn);
		}

		CachedEntry entry = new CachedEntry(getNameCacheKey(segment), newOfferMap,
				Configuration.Cache.NewOfferTTL);
		cachedOfferMap.put(getNameCacheKey(segment), entry);

		logger.debug("resetMap>");
		return newOfferMap;
	}

	private String getNameCacheKey(String segment) {
		String cacheKey = this.getClass() + "::" + "NewOfferCache" + "("
				+ segment + ")";

		return cacheKey;
	}

	
	// Only called by NewOfferGenerator
	@SuppressWarnings("unchecked")
	public Map<String, Row> get(Connection conn, String segment) throws SQLException {
		logger.debug("get<");
		
		Map<String, Row> newOfferMap = null;

		CachedEntry entry = cachedOfferMap.get(getNameCacheKey(segment));

		if ((entry == null) || entry.isExpired()) {
			logger.debug("cache miss");
			newOfferMap = resetMap(conn, segment);
		} else {
			newOfferMap = (Map<String, Row>) entry.getValue();
		}

		logger.debug("get>");
		return newOfferMap;
	}
}
