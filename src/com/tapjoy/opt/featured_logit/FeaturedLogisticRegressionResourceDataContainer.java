package com.tapjoy.opt.featured_logit;

import com.tapjoy.opt.resource.ResourceDataContainer;


public class FeaturedLogisticRegressionResourceDataContainer extends ResourceDataContainer {

	private static FeaturedLogisticRegressionResourceDataContainer instance = new FeaturedLogisticRegressionResourceDataContainer();

	private FeaturedLogisticRegressionResourceDataContainer() {
		this.identity = "FeaturedLogisticRegression";
	}
	
	public static FeaturedLogisticRegressionResourceDataContainer getInstance(){
		return instance;
	}
}