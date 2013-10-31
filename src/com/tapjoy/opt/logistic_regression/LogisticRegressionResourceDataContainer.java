package com.tapjoy.opt.logistic_regression;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.json.simple.parser.ParseException;

import com.tapjoy.opt.resource.ResourceDataContainer;
import com.tapjoy.opt.logistic_regression.config.*;

public class LogisticRegressionResourceDataContainer extends
		ResourceDataContainer {
	public static LogisticRegressionResourceDataContainer instance = new LogisticRegressionResourceDataContainer();
	
	private Logger logger = Logger.getLogger(LogisticRegressionResourceDataContainer.class);
	
	private ContextPredictor _iosPredictor = null;
	private ContextPredictor _andPredictor = null;
	private ContextPredictor _tjcPredictor = null;

	private LogisticRegressionResourceDataContainer() {
		this.identity = "LogisticRegression";
		
		try {
			reloadData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
	}
	
	public static LogisticRegressionResourceDataContainer getInstance(){
		return instance;
	}
	
	public ContextPredictor getContextPredictor(String source, String platform)
	{
		ContextPredictor ret = null;
		if (source.equals("offerwall")) {
			if (platform.equals("iOS")) {
				ret = _iosPredictor;
			} else if (platform.equals("Android")) {
				ret = _andPredictor;
			}
		} else if (source.equals("tj_games")) {
			if (platform.equals("iOS")) {
				ret = _tjcPredictor;
			}
		}
		return ret;
	}
	
	public void reloadData() throws IOException, ParseException
	{
		long start = System.nanoTime();
		_iosPredictor = new ContextPredictor("offerwall", "iOS",
				new File(Configuration.MODEL_DATA_PATH, "model_ios.json.gz").toString(),
				new File(Configuration.MODEL_DATA_PATH, "summary.30.json.gz").toString(),
				new File(Configuration.MODEL_DATA_PATH, "metrics.features.json.gz").toString(),
				new File(Configuration.MODEL_DATA_PATH, "offline.predict.json.gz").toString()
				);
		_andPredictor = new ContextPredictor("offerwall", "Android",
				new File(Configuration.MODEL_DATA_PATH, "model_and.json.gz").toString(),
				new File(Configuration.MODEL_DATA_PATH, "summary.30.json.gz").toString(),
				new File(Configuration.MODEL_DATA_PATH, "metrics.features.json.gz").toString(),
				new File(Configuration.MODEL_DATA_PATH, "offline.predict.json.gz").toString()
				);
			
		_tjcPredictor = new ContextPredictor("tj_games", "iOS",
				new File(Configuration.MODEL_DATA_PATH, "model_tjc.json.gz").toString(),
				new File(Configuration.MODEL_DATA_PATH, "summary.30.json.gz").toString(),
				new File(Configuration.MODEL_DATA_PATH, "metrics.features.json.gz").toString(),
				new File(Configuration.MODEL_DATA_PATH, "offline.predict.json.gz").toString()
				);
		
		_iosPredictor.setRankWeights(Configuration.RANK_WEIGHTS);
		_andPredictor.setRankWeights(Configuration.RANK_WEIGHTS);
		_tjcPredictor.setRankWeights(Configuration.RANK_WEIGHTS);
		
		long end = System.nanoTime();
		
		logger.info(String.format("Date reloaded in %d seconds", TimeUnit.SECONDS.convert(end - start, TimeUnit.NANOSECONDS)));
	}
}
