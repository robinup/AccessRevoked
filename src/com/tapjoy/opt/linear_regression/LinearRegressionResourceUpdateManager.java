package com.tapjoy.opt.linear_regression;

import java.util.HashSet;

import org.apache.log4j.Logger;

import com.tapjoy.opt.linear_regression.config.Configuration;
import com.tapjoy.opt.resource.ResourceDataContainer;
import com.tapjoy.opt.resource.ResourceUpdateManager;
import com.tapjoy.opt.util.S3;

public class LinearRegressionResourceUpdateManager extends
		ResourceUpdateManager {
	protected static Logger logger = Logger.getLogger(LinearRegressionResourceUpdateManager.class);
	
	public LinearRegressionResourceUpdateManager(ResourceDataContainer dataContainer) {
		super(dataContainer);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void reloadDataResource() {
		// Download the 324 files from S3
		logger.info("LinearRegressionResourceUpdateManager :: reloadDataResource ");
		
		S3 s3 = new S3();
		s3.downloadAllFiles(Configuration.S3LocalCopyDir, "tj-optimization", "324", "");
		s3.downloadAllFiles(Configuration.S3LocalCopyDir, "tj-optimization", "330", "");
		s3.downloadAllFiles(Configuration.S3LocalCopyDir, "tj-optimization", "350", "");
		s3.downloadAllFiles(Configuration.S3LocalCopyDir, "tj-optimization", "999", "");

		logger.info("LinearRegressionResourceUpdateManager :: reloadDataResource Done");
	}

	@Override
	protected HashSet<String> getStaticOfferListKeys() {
		return LinearRegressionResourceDataContainer.getInstance().staticOfferLists;
	}

	@Override
	protected void setOfferListKeys(HashSet<String> offerListKeys) {
		LinearRegressionResourceDataContainer.getInstance().staticOfferLists = offerListKeys;
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
