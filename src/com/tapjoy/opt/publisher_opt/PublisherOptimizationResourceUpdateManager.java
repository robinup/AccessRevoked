package com.tapjoy.opt.publisher_opt;


import java.util.HashSet;

import org.apache.log4j.Logger;

import com.tapjoy.opt.publisher_opt.config.Configuration;
import com.tapjoy.opt.resource.ResourceDataContainer;
import com.tapjoy.opt.resource.ResourceUpdateManager;
import com.tapjoy.opt.util.S3;

public class PublisherOptimizationResourceUpdateManager extends
		ResourceUpdateManager {
	protected static Logger logger = Logger.getLogger(PublisherOptimizationResourceUpdateManager.class);
	
	public PublisherOptimizationResourceUpdateManager(ResourceDataContainer dataContainer) {
		super(dataContainer);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void reloadDataResource() {
		// Download the 324 files from S3
		logger.info("PublisherOptimizationResourceUpdateManager :: reloadDataResource ");
		
		S3 s3 = new S3();
		System.out.println("ready to get S3 data for 401");
		s3.downloadAllFiles(Configuration.S3LocalCopyDir, "tj-optimization", "publisher_offerwall", "");
		System.out.printf("got S3 data for 401 in %s\n", Configuration.S3LocalCopyDir);
		//s3.downloadAllFiles(Configuration.S3LocalCopyDir, "tj-optimization", "330", "");
		//s3.downloadAllFiles(Configuration.S3LocalCopyDir, "tj-optimization", "350", "");
		//s3.downloadAllFiles(Configuration.S3LocalCopyDir, "tj-optimization", "999", "");

		logger.info("PublisherOptimizationResourceUpdateManager :: reloadDataResource Done");
	}

	@Override
	protected HashSet<String> getStaticOfferListKeys() {
		return PublisherOptimizationResourceDataContainer.getInstance().staticOfferLists;
	}

	@Override
	protected void setOfferListKeys(HashSet<String> offerListKeys) {
		PublisherOptimizationResourceDataContainer.getInstance().staticOfferLists = offerListKeys;
	}

	@Override
	protected String getIDKey() {
		return Configuration.IDKEY;
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

}
