package com.tapjoy.opt.vertica_score.offerGenerator;

import static org.junit.Assert.assertTrue;

import java.util.Map;
import org.junit.Test;

import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.vertica_score.config.Configuration;
import com.tapjoy.opt.vertica_score.objectCache.AuditionCache;

public class S3AuditionGeneratorTest {

	@Test
	public void auditionList() {
		Map<String, Row> results = AuditionCache.getInstance().getAuditionMap(Configuration.OS.IOS,
				Configuration.Platform.OFFERWALL);
		assertTrue(results.size() > 1000);

		results = AuditionCache.getInstance().getAuditionMap(Configuration.OS.ANDROID,
				Configuration.Platform.OFFERWALL);
		assertTrue(results.size() > 1500);

		results = AuditionCache.getInstance().getAuditionMap(Configuration.OS.IOS,
				Configuration.Platform.TJM);
		assertTrue(results.size() > 1500);

		results = AuditionCache.getInstance().getAuditionMap(Configuration.OS.ANDROID,
				Configuration.Platform.TJM);
		assertTrue(results.size() > 2000);
	}
}
