package com.tapjoy.opt.featured_logit;

import java.util.HashSet;

import org.apache.log4j.Logger;

import com.tapjoy.opt.resource.ResourceDataContainer;
import com.tapjoy.opt.resource.ResourceUpdateManager;
import com.tapjoy.opt.util.S3;
import com.tapjoy.opt.featured_logit.config.*;


public class FeaturedLogisticRegressionResourceUpdateManager extends ResourceUpdateManager {

	protected static Logger logger = Logger.getLogger(FeaturedLogisticRegressionResourceUpdateManager.class);
	
	public FeaturedLogisticRegressionResourceUpdateManager(ResourceDataContainer dataContainer) {
		super(dataContainer);
		// TODO Auto-generated constructor stub
	}
	
	public void reloadDataResource() {
		logger.info("FeaturedLogisticRegressionResourceUpdateManager :: reloadDataResource ");
		//load data
		S3 s3 = new S3();
		s3.downloadAllFiles(Configuration.S3LocalCopyDir, "tj-optimization-test", "justin-test/", "justin-test/");
		logger.info("FeaturedLogisticRegressionResourceUpdateManager :: reloadDataResource Done");
	}
	
	@Override
	protected HashSet<String> getStaticOfferListKeys() {
		return FeaturedLogisticRegressionResourceDataContainer.getInstance().staticOfferLists;
	}

	@Override
	protected void setOfferListKeys(HashSet<String> offerListKeys) {
		FeaturedLogisticRegressionResourceDataContainer.getInstance().staticOfferLists = offerListKeys;
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