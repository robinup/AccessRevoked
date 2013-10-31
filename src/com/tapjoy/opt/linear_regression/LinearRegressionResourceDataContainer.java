package com.tapjoy.opt.linear_regression;

import com.tapjoy.opt.resource.ResourceDataContainer;

public class LinearRegressionResourceDataContainer extends
		ResourceDataContainer {
	// For Linear Regression Engine, we really don't need this container 
	
	public static LinearRegressionResourceDataContainer instance = new LinearRegressionResourceDataContainer();

	private LinearRegressionResourceDataContainer() {
		this.identity = "LinearRegression";
	}
	
	public static LinearRegressionResourceDataContainer getInstance(){
		return instance;
	}
}
