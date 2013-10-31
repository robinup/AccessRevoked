package com.tapjoy.opt.vertica_score.objectCache;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.tapjoy.opt.object_cache.CachedEntry;
import com.tapjoy.opt.vertica_score.config.Configuration;
import com.tapjoy.opt.vertica_score.offerGenerator.S3AuditionGenerator;

/**
 * Caching for All Offer Cache
 * 
 * @author lli
 * 
 */
public class S3AuditionCache {
	private static S3AuditionCache instance = new S3AuditionCache();
	private static Logger logger = Logger.getLogger(S3AuditionCache.class);

	public static Map<String, CachedEntry> cachedMap = new HashMap<String, CachedEntry>();

	public static S3AuditionCache getInstance() {
		return instance;
	}

	private S3AuditionCache() {
	}

	public synchronized void refreshS3Files() {
		S3AuditionGenerator auditionGenerator = new S3AuditionGenerator();
		auditionGenerator.downloadFromS3();

		CachedEntry entry = new CachedEntry(getNameCacheKey(), "",
				Configuration.Cache.S3AuditionTTL);
		cachedMap.put(getNameCacheKey(), entry);
	}

	private String getNameCacheKey() {
		String cacheKey = this.getClass() + "::" + "S3AuditionCache" + "(" + ")";

		return cacheKey;
	}

	public boolean getS3Files() {
		boolean refreshed = false;
		
		CachedEntry entry = cachedMap.get(getNameCacheKey());

		if ((entry == null) || entry.isExpired()) {
			logger.debug("cache miss");
			refreshS3Files();
			refreshed = true;
		}

		return refreshed;
	}
}
