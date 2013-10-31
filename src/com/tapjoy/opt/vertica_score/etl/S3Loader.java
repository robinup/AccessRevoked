package com.tapjoy.opt.vertica_score.etl;

import java.util.Date;

import com.tapjoy.opt.util.DateUtil;
import com.tapjoy.opt.vertica_score.config.Configuration;

/**
 * Loading data files from S3 to Vertica database
 * 
 * @author lli
 * 
 */
public class S3Loader {
	public static String[] OPTIMIZATION_TABLES = { "offerwall_views", "views",
			"clicks", "actions" };

	/**
	 * returning the path for the etl file for today
	 * 
	 * @param tableName
	 * @return
	 */
	public String getEtlPrefix(String tableName) {
		Date today = new Date();
		String path = Configuration.Etl.ETL_SUBFOLDER + "/" + tableName
				+ "/m=" + DateUtil.getYYYY(today) + "-" + DateUtil.getMM(today)
				+ "/d=" + DateUtil.getTodayDateString();

		return path;
	}
}
