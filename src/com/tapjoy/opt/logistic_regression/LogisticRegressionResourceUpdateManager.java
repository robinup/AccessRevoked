package com.tapjoy.opt.logistic_regression;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.json.simple.parser.ParseException;

import com.tapjoy.opt.common.MurmurHash3V2;
import com.tapjoy.opt.resource.ResourceDataContainer;
import com.tapjoy.opt.resource.ResourceUpdateManager;

public class LogisticRegressionResourceUpdateManager extends
		ResourceUpdateManager {

	public LogisticRegressionResourceUpdateManager(
			ResourceDataContainer dataContainer) {
		super(dataContainer);
		// TODO Auto-generated constructor stub
	}


	@Override
	public void reloadDataResource() {
		try {
			LogisticRegressionResourceDataContainer.getInstance().reloadData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected HashSet<String> getStaticOfferListKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void setOfferListKeys(HashSet<String> offerListKeys) {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getIDKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Logger getLogger() {
		// TODO Auto-generated method stub
		return null;
	}

}
