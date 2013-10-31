package com.tapjoy.opt.logistic_regression;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.json.simple.parser.ParseException;

public class SyntheticRankTest extends junit.framework.TestCase {
	
	/*
	private ContextPredictor _predictor = null;
	private SyntheticRank _rank = null;
	
	public void setUp() throws IOException, ParseException
	{
		_predictor = new ContextPredictor(
				"", "",
				"./model.json.gz",
				"./summary.30.json.gz",
				"./metrics.features.json.gz",
				"./offline.predict.json.gz"
				);
		
		_rank = new SyntheticRank(_predictor);
	}
	
	public void tearDown()
	{
		_predictor = null;
		_rank = null;
	}
	
	
	public void testCalculateSytheticCVR()
	{
		double cvr = _rank.calculateSytheticCVR(
				"offerwall", 
				"Android", 
				"android", 
				"CN", 
				"kunming", 
				"zh", 
				50, 
				"6e1127b3-2ec0-462d-a4af-9214d1f31e9e", 
				"7b8db33e-fdc3-4bc4-b593-50d4e12a790e", 
				"");
	}
	
	public static void main(String[] args)
	{
		SyntheticRankTest test = new SyntheticRankTest();
		try {
			System.out.println("setUp");
			
			test.setUp();
			
			long start = System.nanoTime();
			System.out.println(start);
			
			for (int i = 0; i < 1000000; i ++) {
				test.testCalculateSytheticCVR();
			}
			
			long end = System.nanoTime();
			System.out.println(end);
			
			System.out.println((end - start) / 1000000.0);
			System.out.println(TimeUnit.MILLISECONDS.convert(end - start, TimeUnit.NANOSECONDS));
			
			test.tearDown();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}*/
}
