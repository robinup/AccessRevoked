package com.tapjoy.opt.vertica_score.offerGenerator;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.util.Encrypt;
import com.tapjoy.opt.vertica_score.config.Configuration;
import com.tapjoy.opt.vertica_score.offerGenerator.AuditionGenerator;

public class AuditionGeneratorTest {
	private static Logger logger = Logger.getLogger(Encrypt.class);

	@Test
	public void absolutePath() {
		String workingDir = "bin/com/tapjoy/opt/data/audition";

		File myFile = new File(workingDir);
		logger.debug(myFile.getAbsolutePath());

		File[] files = new File(workingDir).listFiles();
		assertEquals(8, files.length);
	}

	@Test
	public void getPlatform() {
		AuditionGenerator generator = new AuditionGenerator(
				"bin/com/tapjoy/opt/data/audition");

		assertEquals(
				Configuration.OS.ANDROID + "_"
						+ Configuration.Platform.OFFERWALL,
				generator
						.getOSPlatformStr("bin/com/tapjoy/opt/data/audition/gen_Android_audition_predict.discount"));
		assertEquals(
				Configuration.OS.IOS + "_" + Configuration.Platform.OFFERWALL,
				generator
						.getOSPlatformStr("bin/com/tapjoy/opt/data/audition/gen_iOS_audition_predict.discount"));
		assertEquals(
				Configuration.OS.ANDROID + "_" + Configuration.Platform.TJM,
				generator
						.getOSPlatformStr("bin/com/tapjoy/opt/data/audition/tjm_Android_audition_predict.discount"));
		assertEquals(
				Configuration.OS.IOS + "_" + Configuration.Platform.TJM,
				generator
						.getOSPlatformStr("bin/com/tapjoy/opt/data/audition/tjm_iOS_audition_predict.discount"));
	}

	@Test
	public void auditionList() {
		AuditionGenerator generator = new AuditionGenerator(
				"bin/com/tapjoy/opt/data/audition");

		Map<String, Row> results = generator.getOffer(Configuration.OS.IOS,
				Configuration.Platform.OFFERWALL);
		assertEquals(1779, results.size());

		results = generator.getOffer(Configuration.OS.ANDROID,
				Configuration.Platform.OFFERWALL);
		assertEquals(1769, results.size());

		results = generator.getOffer(Configuration.OS.IOS,
				Configuration.Platform.TJM);
		assertEquals(1732, results.size());

		results = generator.getOffer(Configuration.OS.ANDROID,
				Configuration.Platform.TJM);
		assertEquals(2221, results.size());
	}
}
