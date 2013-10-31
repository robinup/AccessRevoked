package com.tapjoy.opt.logistic_regression;

import java.io.IOException;

import org.json.simple.parser.ParseException;

import junit.framework.TestCase;

public class ContextPredictorTest extends TestCase {
	
	private ContextPredictor _predictor = null;
	
	public void setUp() throws IOException, ParseException
	{
		_predictor = new ContextPredictor(
				"", "",
				"./model.json.gz",
				"./summary.30.json.gz",
				"./metrics.features.json.gz",
				"./offline.predict.json.gz"
				);
	}
	
	public void tearDown()
	{
		_predictor = null;
	}
	
	public void testPredict()
	{
		/*
		 * {'exchange_rate': 50, 
		 * 'exchange_rate_log': 3.0, 
		 * 'device_type': u'android', 
		 * 'city': u'kunming', 
		 * 'hour': u'8', 
		 * 'language': u'zh', 
		 * 'platform': u'Android', 
		 * 'country': u'CN', 
		 * 'ampm': u'AM', 
		 * 'daytime': 0, 
		 * 'source': u'offerwall', 
		 * 'weekday': 1, 
		 * 'dow': u'Fri', 
		 * 'offerwall_rank': '21'}
		 */
		double pred1 = _predictor.predict(
				"offerwall", 
				"Android", 
				"android", 
				"CN", 
				"kunming", 
				"zh", 
				21,
				21,
				true,
				"Fri",
				"8",
				false,
				"AM",
				50, 
				"6e1127b3-2ec0-462d-a4af-9214d1f31e9e", 
				"7b8db33e-fdc3-4bc4-b593-50d4e12a790e", 
				"");
		assertEquals(0.000162571558119222, pred1, 0.000000001);
		
		/*
		 * {'exchange_rate': 3000, 
		 * 'exchange_rate_log': 8.0, 
		 * 'device_type': u'android', 
		 * 'city': u'kaohsiung', 
		 * 'hour': u'17', 
		 * 'language': u'zh', 
		 * 'platform': u'Android', 
		 * 'country': u'TW', 
		 * 'ampm': u'PM', 
		 * 'daytime': 0, 
		 * 'source': u'offerwall', 
		 * 'weekday': 1, 
		 * 'dow': u'Fri', 
		 * 'offerwall_rank': '9'}
		 */
		double pred2 = _predictor.predict(
				"offerwall", 
				"Android", 
				"android", 
				"TW", 
				"kaohsiung", 
				"zh", 
				9,
				9,
				true,
				"Fri",
				"17",
				false,
				"PM",
				3000, 
				"83403625-a2d4-4080-b4c3-5fa78ad0b729", 
				"5267b5ca-21be-4e22-b1cc-e964e2babfa1", 
				"");
		assertEquals(0.002509630524863813, pred2, 0.000000001);
		
	}
}
