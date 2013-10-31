package com.tapjoy.opt.logistic_regression;

import java.io.IOException;

import org.json.simple.parser.ParseException;

import junit.framework.TestCase;

public class DataPackageTest extends TestCase {
	private DataPackage _package = null;
	
	public void setUp() throws IOException, ParseException
	{
		_package = new DataPackage(
				"", "",
				"./model.json.gz",
				"./summary.30.json.gz",
				"./metrics.features.json.gz",
				"./offline.predict.json.gz"
				);
	}
	
	public void tearDown()
	{
		_package = null;
	}
	
	public void testGetCoefficents()
	{
		assertEquals(_package.getCoefficient("context^source=offerwall"), -0.042396);
	}
	
}
