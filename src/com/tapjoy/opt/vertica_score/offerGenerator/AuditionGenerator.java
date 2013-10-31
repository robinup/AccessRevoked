package com.tapjoy.opt.vertica_score.offerGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.RowFactory;
import com.tapjoy.opt.vertica_score.config.Configuration;

public class AuditionGenerator implements ColumnDef {
	private String workingDir;

	private Map<String, Map<String, Row>> auditionMap;
	private static Logger logger = Logger.getLogger(AuditionGenerator.class);
	private static String[] auditionColumns_discount = { OFFER_ID,
		PREDITION_SCORE, BID, RREDICTIION_RANK, RREDICTIION_RANK_DISCOUNT,
		PREDICTION_SCORE_DISCOUNT, BID_DISCOUNT };
	private static String[] auditionColumns = { OFFER_ID, PREDITION_SCORE };
	
	private RowFactory auditionRowFactory;

	private static String GEN_IOS = "gen_ios";
	private static String GEN_ANDROID = "gen_android";
	private static String TJM_IOS = "tjm_ios";
	private static String TJM_ANDROID = "tjm_android";

	AuditionGenerator(String workingDir) {
		this.workingDir = workingDir;
		auditionMap = new HashMap<String, Map<String, Row>>();

		if (Configuration.RUN_BID_DISCOUNT) {
			auditionRowFactory = new RowFactory(auditionColumns_discount);
		} else {
			auditionRowFactory = new RowFactory(auditionColumns);
		}
	}

	/**
	 * using system wide configuration
	 */
	AuditionGenerator() {
		this.workingDir = Configuration.getAuditionDir();
		auditionMap = new HashMap<String, Map<String, Row>>();

		if (Configuration.RUN_BID_DISCOUNT) {
			auditionRowFactory = new RowFactory(auditionColumns_discount);
		} else {
			auditionRowFactory = new RowFactory(auditionColumns);
		}
	}

	/**
	 * returning OS based on file name
	 * 
	 * @param fileName
	 * @return
	 */
	public String getOSPlatformStr(String fileName) {
		logger.debug("getOSPlatformStr<");
		

		String osPlatform = null;

		if (fileName != null) {
			// To filter out the current audition file (temporarily)
			if (Configuration.RUN_BID_DISCOUNT) {
				if (!fileName.toLowerCase().matches(".*discount")) {
					return null;
				}
			} else {
				if ( fileName.toLowerCase().matches(".*discount")) {
					return null;
				}
			}
			
			
			if (fileName.toLowerCase().matches(".*" + GEN_IOS + ".*")) {
				osPlatform = Configuration.OS.IOS + "_"
						+ Configuration.Platform.OFFERWALL;
			} else if (fileName.toLowerCase()
					.matches(".*" + GEN_ANDROID + ".*")) {
				osPlatform = Configuration.OS.ANDROID + "_"
						+ Configuration.Platform.OFFERWALL;
			} else if (fileName.toLowerCase().matches(".*" + TJM_IOS + ".*")) {
				osPlatform = Configuration.OS.IOS + "_"
						+ Configuration.Platform.TJM;
			} else if (fileName.toLowerCase()
					.matches(".*" + TJM_ANDROID + ".*")) {
				osPlatform = Configuration.OS.ANDROID + "_"
						+ Configuration.Platform.TJM;
			}
		}

		logger.debug("getOSPlatformStr>");
		return osPlatform;
	}

	/**
	 * processing the audition file putting the result into the list
	 * 
	 * @throws FileNotFoundException
	 */
	private void processFile(File file, Map<String, Row> rows) {
		String line;
		BufferedReader reader = null;
		logger.debug("processFile<");
	
		try {
			reader = new BufferedReader(new FileReader(file));

			while ((line = reader.readLine()) != null) {
				// ignore empty line
				if (line.length() == 0) {
					logger.trace("empty line");
					continue;
				}

				String[] results = line.split(",");

				if (Configuration.RUN_BID_DISCOUNT) {
					if (results.length != 8) {
						logger.debug("the line does not have exactly 3 fields");
						continue;
					}
				} else {
					if (results.length != 4) {
						logger.debug("the line does not have exactly 3 fields");
						continue;
					}
				}

				// Ignoring the offer with pred_score <=0 and not empty
				if (Double.valueOf(results[3]) <= 0.0) {
					logger.debug("pred_score has less than 0.0 value");
					continue;
				}
				
				Row row = auditionRowFactory.newRow();

				if (Configuration.RUN_BID_DISCOUNT) {
					row.setColumn(OFFER_ID, results[0]);
					row.setColumn(BID, results[2]);
					row.setColumn(PREDITION_SCORE, results[3]);
					row.setColumn(RREDICTIION_RANK, results[4]);
					row.setColumn(BID_DISCOUNT, results[5]);
					row.setColumn(PREDICTION_SCORE_DISCOUNT, results[6]);
					row.setColumn(RREDICTIION_RANK_DISCOUNT, results[7]);
				} else {
					row.setColumn(OFFER_ID, results[0]);
					row.setColumn(PREDITION_SCORE, results[3]);
				}

				// adding into result set
				rows.put(row.getColumn(OFFER_ID), row);
			}

			logger.debug("processFile>");
		} catch (FileNotFoundException e) {
			logger.error("processFile failed!!!", e);
		} catch (IOException e) {
			logger.error("processFile failed!!!", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					logger.error("processFile failed!!!", e);
				}
			}
		}
	}

	public void processAudition() {
		logger.debug("processAudition<");
		
		File[] files = new File(this.workingDir).listFiles();

		// getting all the files
		for (File file : files) {
			// ignoring not plain file
			if (file.isFile() == false) {
				//logger.error("the file is not plain file " + file.getName());
				continue;
			}

			String platform = getOSPlatformStr(file.getName());
			// ignoring file not with part of platform files
			if (platform == null) {
				logger.error("unknow platform for file " + file.getName());
				continue;
			}

			Map<String, Row> auditionList = auditionMap.get(platform);
			if (auditionList == null) {
				auditionList = new HashMap<String, Row>();
				auditionMap.put(platform, auditionList);
			}

			processFile(file, auditionList);
		}

		logger.debug("processAudition>");
	}

	/**
	 * returning the map of audition offers The key fields are offer_id, os,
	 * platform The value field is pred_score "os" field has "IOS" OR "ANDROID"
	 * values "platform" field has "IAO" OR "TJM" values
	 * 
	 * Ignoring the offer with pred_score <=0 and not empty
	 */
	public Map<String, Row> getOffer(String os, Integer platform) {
		logger.debug("getOffer<");

		// processing all the audition files
		processAudition();
		Map<String, Row> auditionResult = auditionMap.get(os + "_" + platform);

		logger.debug("getOffer>");
		return auditionResult;
	}
}
