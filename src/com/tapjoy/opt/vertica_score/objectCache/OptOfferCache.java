package com.tapjoy.opt.vertica_score.objectCache;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.OfferRowUtil;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.object_cache.CachedEntry;
import com.tapjoy.opt.vertica_score.config.Configuration;
import com.tapjoy.opt.vertica_score.config.ConfigurationSegment;
import com.tapjoy.opt.vertica_score.etl.sql.app.AppOPTOffer;
import com.tapjoy.opt.vertica_score.etl.sql.country.CountryOPTOffer;
import com.tapjoy.opt.vertica_score.etl.sql.countryapp.CountryAppOPTOffer;
import com.tapjoy.opt.vertica_score.etl.sql.global.GlobalOPTOffer;
import com.tapjoy.opt.vertica_score.etl.sql.tjmcountry.TJMCountryOPTOffer;
import com.tapjoy.opt.vertica_score.etl.sql.tjmglobal.TJMOPTOffer;

/**
 * Caching for All Offer Cache
 * 
 * @author lli
 * 
 */
public class OptOfferCache {
	private static OptOfferCache instance = new OptOfferCache();
	private static Logger logger = Logger.getLogger(OptOfferCache.class);

	public static Map<String, CachedEntry> cachedOfferMap = new HashMap<String, CachedEntry>();

	public static OptOfferCache getInstance() {
		return instance;
	}

	private OptOfferCache() {
	}

	public synchronized Map<String, Row> resetMap(Connection conn,
			String segment) throws SQLException {

		Map<String, Row> offerMap = new HashMap<String, Row>();

		if (segment.equals(ConfigurationSegment.OPT_GOW_GLOBAL)) {
			offerMap = GlobalOPTOffer.getOfferMap(conn);
		} else if (segment.equals(ConfigurationSegment.OPT_GOW_COUNTRY)) {
			offerMap = CountryOPTOffer.getOfferMap(conn);
		} else if (segment.equals(ConfigurationSegment.OPT_GOW_APP)) {
			offerMap = AppOPTOffer.getOfferMap(conn);
		} else if (segment.equals(ConfigurationSegment.OPT_GOW_COUNTRY_APP)) {
			offerMap = CountryAppOPTOffer.getOfferMap(conn);
		} else if (segment.equals(ConfigurationSegment.OPT_TJM)) {
			offerMap = TJMOPTOffer.getOfferMap(conn);
		} else if (segment.equals(ConfigurationSegment.OPT_TJM_COUNTRY)) {
			offerMap = TJMCountryOPTOffer.getOfferMap(conn);
		}

		CachedEntry entry = new CachedEntry(getNameCacheKey(segment), offerMap,
				Configuration.Cache.NewOfferTTL);
		cachedOfferMap.put(getNameCacheKey(segment), entry);

		return offerMap;
	}

	private String getNameCacheKey(String segment) {
		String cacheKey = this.getClass() + "::" + "OptOfferCache" + "("
				+ segment + ")";

		return cacheKey;
	}

	// Used by OptOfferGeneratorBase -- updateOfferMap
	@SuppressWarnings("unchecked")
	public Map<String, Row> get(Connection conn, String segment)
			throws SQLException {
		Map<String, Row> offerMap = null;

		CachedEntry entry = cachedOfferMap.get(getNameCacheKey(segment));

		if ((entry == null) || entry.isExpired()) {
			logger.debug("cache miss");
			offerMap = resetMap(conn, segment);
		} else {
			offerMap = (Map<String, Row>) entry.getValue();
		}

		offerMap = OfferRowUtil.cloneRowMap(offerMap);
		if (logger.isDebugEnabled()) {
			OfferRowUtil.debugOfferRow(offerMap.values(),
					"In the OPT Offer Map for " + segment);
		}

		return offerMap;
	}
}
