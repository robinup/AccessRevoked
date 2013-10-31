package com.tapjoy.opt.vertica_score.offerGenerator;

import java.util.Map;
import org.apache.log4j.Logger;

import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.util.S3;
import com.tapjoy.opt.vertica_score.config.Configuration;

public class S3AuditionGenerator {
	private S3 s3;
	private String directory;
	private AuditionGenerator generator;
	private static Logger logger = Logger.getLogger(S3AuditionGenerator.class);

	public S3AuditionGenerator() {
		s3 = new S3();
		directory = Configuration.getAuditionDir();
		generator = new AuditionGenerator(directory);
	}

	/**
	 * Downloading files from S3
	 */
	public void downloadFromS3() {
		logger.debug("downloadFromS3<");

		// downloading current audition file to local audition directory
		s3.downloadAllFiles(directory, Configuration.getAuditionS3Bucket(),
				Configuration.getAuditionDirPrefix(), Configuration.getAuditionDirPrefix());
		
		logger.debug("downloadFromS3>");		
	}
	
	/**
	 * returning the map of audition offers The key fields are offer_id, os,
	 * platform The value field is pred_score "os" field has "IOS" OR "ANDROID"
	 * values "platform" field has "IAO" OR "TJM" values
	 * 
	 * Ignoring the offer with pred_score <=0 and not empty
	 */
	public Map<String, Row> getOffer(String os, Integer platform) {		
		return generator.getOffer(os, platform);
	}
}
