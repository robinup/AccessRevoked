package com.tapjoy.opt.logistic_regression;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import org.javatuples.*;
import org.json.simple.*;
import org.json.simple.parser.*;

public class DataPackage {
	private String _source = null;
	private String _platform = null;
	private String _segment = null;
	private String _modelPath = null;
	private String _summaryPath = null;
	private String _metricsPath = null;
	private String _offlinePath = null;
	
	private HashMap<String, Double> _coefs = new HashMap<String, Double>(1000000);
	private HashMap<String, Triplet<Double, Double, Boolean>> _summary = new HashMap<String, Triplet<Double, Double, Boolean>>(500000);
	private HashMap<String, Double> _metrics = new HashMap<String, Double>(1000000);
	private HashMap<String, Double> _offers = new HashMap<String, Double>(10000);
	private HashMap<String, Double> _apps = new HashMap<String, Double>(10000);
	
	public DataPackage(String source, String platform, String modelPath, String summaryPath, String metricsPath, String offlinePath) throws IOException, IOException, ParseException
	{
		_source = source;
		_platform = source;
		_segment = source + '.' + platform;
		_modelPath = modelPath;
		_summaryPath = summaryPath;
		_metricsPath = metricsPath;
		_offlinePath = offlinePath;
		
		populate();
	}
	
	private BufferedReader openFileReader(String filename) throws IOException
	{
		InputStream fileStream = new FileInputStream(filename);
		InputStream gzipStream = new GZIPInputStream(fileStream);
		Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
		BufferedReader buffered = new BufferedReader(decoder);
		
		return buffered;
	}
	
	private Object parseJsonFile(String filename) throws IOException, ParseException
	{
		JSONParser parser = new JSONParser();
		Reader reader = openFileReader(filename);
		Object obj = parser.parse(reader);
		return obj;
	}
	
	private void populate() throws FileNotFoundException, IOException, ParseException
	{
		JSONObject modelObject = (JSONObject)((JSONObject)parseJsonFile(_modelPath)).get("coefficients");
		
		_coefs.put("intercept", (Double)((JSONObject)modelObject.get("1")).get("coef"));
		for (Object key : modelObject.keySet()) {
			JSONObject m = (JSONObject)modelObject.get(key);
			double coef = (Double)m.get("coef");
			
			JSONArray features = (JSONArray)m.get("features");
			for (Object fea : features) {
				_coefs.put((String)fea, coef);
			}
		}

		JSONObject summaryObject = (JSONObject)parseJsonFile(_summaryPath);

		for (Object key : summaryObject.keySet()) {
			JSONObject s = (JSONObject)summaryObject.get(key);
			double mean = (Double)s.get("mean");
			double std = (Double)s.get("std");
			boolean indicator = (Long)s.get("indicator") != 0;
			
			_summary.put((String)key, new Triplet<Double, Double, Boolean>(mean, std, indicator));
		}
		
		JSONObject offlineObject = (JSONObject)parseJsonFile(_offlinePath);
		JSONObject offerObject = (JSONObject)offlineObject.get("offers");
		JSONObject appObject = (JSONObject)offlineObject.get("apps");
		
		for (Object key : offerObject.keySet()) {
			if (key.equals("__min__")) {
				continue;
			}
			JSONObject offer = (JSONObject)offerObject.get(key);
			_offers.put((String)key, (Double)offer.get(_segment));
		}
		
		for (Object key : appObject.keySet()) {
			if (key.equals("__min__")) {
				continue;
			}
			JSONObject app = (JSONObject)appObject.get(key);
			_apps.put((String)key, (Double)app.get(_segment));
		}
		
		JSONObject metricsObject = (JSONObject)parseJsonFile(_metricsPath);
		for (Object key : metricsObject.keySet()) {
			JSONObject metric = (JSONObject)metricsObject.get(key);
			
			for (Object e: metric.keySet()) {
				String tmp = (String)e;
				String[] splitted = tmp.split(":");
				if (_source == splitted[0] && _platform == splitted[1]) {
					JSONObject element = (JSONObject)metric.get(e);
					double cvr = (Double)element.get("cvr");
					if (cvr == 0.0) {
						continue;
					}
					String k = String.format("%s:%s", key, splitted[2]);
					_metrics.put(k, cvr);
				}
			}
		}
	}
	
	public double getCoefficient(String fea)
	{
		Double ret = _coefs.get(fea);
		return (ret != null) ? ret : 0.0;
	}
	
	public Triplet<Double, Double, Boolean> getSummary(String fea)
	{
		Triplet<Double, Double, Boolean> ret = _summary.get(fea);
		return ret;
	}
	
	public double getMetrics(String dimension, String element)
	{
		Double ret = _metrics.get(dimension + ":" + element);
		return (ret != null) ? ret : 0.0;
	}
	
	public double getOfferPrediction(String offerId)
	{
		Double ret = _offers.get(offerId);
		return (ret != null) ? ret : 0.0;
	}
	
	public double getAppPrediction(String appId)
	{
		Double ret = _apps.get(appId);
		return (ret != null) ? ret : 0.0;
	}
}
