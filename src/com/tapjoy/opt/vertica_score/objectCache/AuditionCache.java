package com.tapjoy.opt.vertica_score.objectCache;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.OfferRowUtil;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.object_cache.CachedEntry;
import com.tapjoy.opt.vertica_score.config.Configuration;
import com.tapjoy.opt.vertica_score.offerGenerator.S3AuditionGenerator;

/**
 * Caching for All Offer Cache
 * 
 * @author lli
 * 
 */
public class AuditionCache {
	private static AuditionCache instance = new AuditionCache();
	private static Logger logger = Logger.getLogger(AuditionCache.class);

	public static Map<String, CachedEntry> cachedMap = new HashMap<String, CachedEntry>();

	public static AuditionCache getInstance() {
		return instance;
	}

	private AuditionCache() {
	}

	public synchronized CachedEntry refreshAuditionMap(String os,
			Integer platform) {
		S3AuditionGenerator auditionGenerator = new S3AuditionGenerator();

		Map<String, Row> results = auditionGenerator.getOffer(os, platform);
		CachedEntry entry = new CachedEntry(getNameCacheKey(os, platform),
				results, Configuration.Cache.S3AuditionTTL);

		cachedMap.put(getNameCacheKey(os, platform), entry);

		return entry;
	}

	private String getNameCacheKey(String os, Integer platform) {
		String cacheKey = this.getClass() + "::" + "AuditionCache" + "(" + os
				+ "," + platform + ")";

		return cacheKey;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Row> getAuditionMap(String os, Integer platform) {
		logger.debug("getAuditionMap<");

		// refreshing files from S3 as needed
		boolean refreshed = false;
		
		System.out.printf("this is getAuditionMap --- LJ \n");

		if (Configuration.Audition.LOAD_S3) {
			refreshed = S3AuditionCache.getInstance().getS3Files();
			System.out.printf("refreshed retrived from S3 --- LJ \n");
		}

		CachedEntry entry = cachedMap.get(getNameCacheKey(os, platform));
		System.out.printf("got cacheMap entry--- LJ \n");
		if (refreshed || (entry == null) || entry.isExpired()) {
			logger.debug("cache miss");
			entry = refreshAuditionMap(os, platform);
			System.out.printf("AuditionMap refreshed --- LJ \n");
		}

		Map<String, Row> auditionMap = (Map<String, Row>) entry.getValue();
		auditionMap = OfferRowUtil.cloneRowMap(auditionMap);
		System.out.printf("RowMap cloned for auditionMap --- LJ \n");

		logger.debug("getAuditionMap>");
		return auditionMap;
	}
	
}
