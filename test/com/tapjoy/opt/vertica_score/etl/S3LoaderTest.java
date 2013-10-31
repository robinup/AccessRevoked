package com.tapjoy.opt.vertica_score.etl;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Ignore;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.tapjoy.opt.util.S3;
import com.tapjoy.opt.vertica_score.config.Configuration;
import com.tapjoy.opt.vertica_score.etl.S3Loader;

import static org.junit.Assert.*;

public class S3LoaderTest {
	private static Logger logger = Logger.getLogger(S3LoaderTest.class);

	@Ignore
	public void listBucketWithMD5() {
		S3Loader s3Loader = new S3Loader();
		String prefix = s3Loader.getEtlPrefix("views");
		assertTrue(prefix.startsWith("etl_main/views"));

		S3 s3 = new S3();
		List<S3ObjectSummary> fileNames = s3.listBucketSummary(Configuration.Etl.ETL_BUCKET,
				"etl_main/views/m=2012-11/d=2012-11-06/b=23");
		assertEquals(184, fileNames.size());
		fileNames = s3.listBucketSummary(Configuration.Etl.ETL_BUCKET,
				"etl_main/views/m=2012-11/d=2012-11-06");
		assertEquals(3414, fileNames.size());
		
		for (S3ObjectSummary summary : fileNames) {
			logger.trace("summary:bucket name:" + summary.getBucketName());
			logger.error("summary:key:" + summary.getKey());
			
			//ETag is hash value of file!!!
			logger.error("summary:ETag:" + summary.getETag());  
			//logger.error("summary:size:" + summary.getSize());
			//logger.error("summary:storage class:" + summary.getStorageClass());
			logger.error("summary:getLastModified:" + summary.getLastModified());
			//logger.error("summary:getOwner:" + summary.getOwner());
		} 
		
	}	

	@Ignore
	public void downloadViews() {
		S3Loader s3Loader = new S3Loader();
		String prefix = s3Loader.getEtlPrefix("views");
		assertTrue(prefix.startsWith("etl_main/views"));

		S3 s3 = new S3();
				
		s3.downloadAllFiles("bin/s3download", Configuration.Etl.ETL_BUCKET,
				"etl_main/actions/m=2012-11/d=2012-11-06/b=234", "etl_main/");

		s3.downloadAllFiles("bin/s3download", Configuration.Etl.ETL_BUCKET,
				"etl_main/offerwall_views/m=2012-11/d=2012-11-06/b=234", "etl_main/");
	}	
}
