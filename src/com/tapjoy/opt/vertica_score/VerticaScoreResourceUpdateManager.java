package com.tapjoy.opt.vertica_score;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import net.spy.memcached.MemcachedClient;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;

import com.tapjoy.opt.GlobalDataManager;
import com.tapjoy.opt.cache.OfferListCache;
import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.EmptyRankedScoreException;
import com.tapjoy.opt.common.OfferRowUtil;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.object_cache.OfferCache;
import com.tapjoy.opt.offerlist.OfferList;
import com.tapjoy.opt.offerlist.OfferListFactory;
import com.tapjoy.opt.offerlist.OfferListRowMC;
import com.tapjoy.opt.resource.ResourceDataContainer;
import com.tapjoy.opt.resource.ResourceManager;
import com.tapjoy.opt.resource.ResourceUpdateManager;
import com.tapjoy.opt.util.CommandExecutor;
import com.tapjoy.opt.util.DateUtil;
import com.tapjoy.opt.util.MClient;
import com.tapjoy.opt.util.StringUtil;
import com.tapjoy.opt.vertica_score.config.Configuration;
import com.tapjoy.opt.vertica_score.config.ConfigurationApp;
import com.tapjoy.opt.vertica_score.config.ConfigurationCountries;
import com.tapjoy.opt.vertica_score.entity.RankedOfferKey;
import com.tapjoy.opt.vertica_score.etl.sql.OfferWallAction;
import com.tapjoy.opt.vertica_score.etl.sql.OfferWallActionAgg;
import com.tapjoy.opt.vertica_score.etl.sql.OfferWallViewAgg;
import com.tapjoy.opt.vertica_score.etl.sql.Opt2Test;
import com.tapjoy.opt.vertica_score.etl.sql.app.AppOfferAgg;
import com.tapjoy.opt.vertica_score.etl.sql.country.CountryOfferAgg;
import com.tapjoy.opt.vertica_score.etl.sql.countryapp.CountryAppOfferAgg;
import com.tapjoy.opt.vertica_score.etl.sql.global.GlobalOfferAgg;
import com.tapjoy.opt.vertica_score.etl.sql.tjmcountry.TJMCountryOfferAgg;
import com.tapjoy.opt.vertica_score.etl.sql.tjmglobal.TJMOfferAgg;
import com.tapjoy.opt.vertica_score.objectCache.AuditionCache;
import com.tapjoy.opt.vertica_score.offerGenerator.OptOfferFileWriter;
import com.tapjoy.opt.vertica_score.offerGenerator.OptOfferGenerator;
import com.tapjoy.opt.vertica_score.offerGenerator.RankingUtil;
import com.tapjoy.opt.vertica_score.offerGenerator.app.AppOptOfferGenerator;
import com.tapjoy.opt.vertica_score.offerGenerator.country.CountryOptOfferGenerator;
import com.tapjoy.opt.vertica_score.offerGenerator.countryapp.CountryAppOptOfferGenerator;
import com.tapjoy.opt.vertica_score.offerGenerator.global.GlobalOptOfferGenerator;
import com.tapjoy.opt.vertica_score.offerGenerator.tjmcountry.TJMCountryOptOfferGenerator;
import com.tapjoy.opt.vertica_score.offerGenerator.tjmglobal.TJMGlobalOptOfferGenerator;
import com.tapjoy.opt.vertica_score.target.app.etl.AppInstallAgg;

@SuppressWarnings("unused")
public class VerticaScoreResourceUpdateManager extends ResourceUpdateManager implements ColumnDef {
	private Map<RankedOfferKey, List<Row>> rankedOfferMap;
	protected static Logger logger = Logger.getLogger(VerticaScoreResourceUpdateManager.class);

	private int refreshMapRunCount = 0;

	
	public VerticaScoreResourceUpdateManager(ResourceDataContainer dataContainer) {
		super(dataContainer);
	}

	/**
	 * refresh the audition files
	 * 
	 * @param current
	 */
	private String refreshAudition(Date current) {
		String line = null;
		String outputPath = RankingUtil.getAudtionOutputDir(current) + "/";
		logger.debug("refreshAudition<");

		try {
			URL location = VerticaScoreResourceUpdateManager.class
					.getProtectionDomain().getCodeSource().getLocation();
			String currentPath = location.getFile();

			currentPath = System.getProperty("user.dir") + "/";

			// making the directory is not available
			File file = new File(currentPath + outputPath);
			File parent_directory = file.getParentFile();
			if (null != parent_directory) {
				parent_directory.mkdirs();
			}

			// Generating the Auditon list
			CommandExecutor.executeCommand("python " + currentPath
					+ "auditioning/ver2.0/auditioning_adaboost_pipe.py "
					+ currentPath + outputPath, logger);
			
			
			

			// AuditionDiscount -- Generating the Discounted Audition List
			if (Configuration.RUN_BID_DISCOUNT) {
				CommandExecutor.executeCommand("python " + currentPath
						+ "auditioning/bid_discount/discount_main.py -v -I "
						+ currentPath + outputPath + "current/ -O "
						+ currentPath + outputPath + "current/  2>&1", logger);

				// Copy results -- have to copy files one by one.
				CommandExecutor.executeCommand("cp " + currentPath + outputPath
						+ "current/gen_Android_audition_predict.discount" + " "
						+ currentPath + outputPath + "../", logger);

				CommandExecutor.executeCommand("cp " + currentPath + outputPath
						+ "current/gen_iOS_audition_predict.discount" + " "
						+ currentPath + outputPath + "../", logger);

				CommandExecutor.executeCommand("cp " + currentPath + outputPath
						+ "current/tjm_Android_audition_predict.discount" + " "
						+ currentPath + outputPath + "../", logger);

				CommandExecutor.executeCommand("cp " + currentPath + outputPath
						+ "current/tjm_iOS_audition_predict.discount" + " "
						+ currentPath + outputPath + "../", logger);
			} else {
				// Copy results -- have to copy files one by one.
				CommandExecutor.executeCommand("cp " + currentPath + outputPath
						+ "current/gen_Android_audition_predict" + " "
						+ currentPath + outputPath + "../", logger);

				CommandExecutor.executeCommand("cp " + currentPath + outputPath
						+ "current/gen_iOS_audition_predict" + " "
						+ currentPath + outputPath + "../", logger);

				CommandExecutor.executeCommand("cp " + currentPath + outputPath
						+ "current/tjm_Android_audition_predict" + " "
						+ currentPath + outputPath + "../", logger);

				CommandExecutor.executeCommand("cp " + currentPath + outputPath
						+ "current/tjm_iOS_audition_predict" + " "
						+ currentPath + outputPath + "../", logger);
			}

			logger.debug("refreshAudition>");
			return outputPath;
		} catch (IOException e) {
			logger.fatal("audition process IO process failer", e);
			return null;
		} catch (InterruptedException e) {
			logger.fatal("audition process IO process interrupted", e);
			return null;
		}

	}

	/**
	 * Updating offer wall actions/views aggregation
	 * 
	 * @param conn
	 * @throws SQLException
	 */
	public void todayETLAgg(Connection conn) throws SQLException {
		logger.debug("todayETLAgg<");

		String todayDate = DateUtil.getTodayDateString();

		// whether copying today's etl data from optimization database to
		// opt_test
		if (Configuration.FROM_OPT_TO_TEST) {
			Opt2Test.execute(conn, todayDate);
		}

		// updating offerwall action table
		OfferWallAction.execute(conn, todayDate);

		// aggregating view table
		OfferWallViewAgg.execute(conn, todayDate);

		// aggregating action table
		OfferWallActionAgg.execute(conn, todayDate);

		logger.debug("todayETLAgg>");
	}

	public synchronized Map<RankedOfferKey, List<Row>> getRankedOfferMap() {
		Map<RankedOfferKey, List<Row>> currRankedOfferMap;

		synchronized (this) {
			currRankedOfferMap = this.rankedOfferMap;
		}

		return currRankedOfferMap;
	}

	public void refreshRankedOfferMap(Connection conn) throws SQLException,
			IOException {
		HashMap<RankedOfferKey, List<Row>> rankedOfferMap = generate(conn);

		synchronized (this) {
			this.rankedOfferMap = rankedOfferMap;
		}
	}

	/*
	 * foreach map entry, creating sorted offerId/adjusted_score
	 */
	public HashMap<RankedOfferKey, List<Row>> generate(Connection conn)
			throws SQLException, IOException {
		HashMap<RankedOfferKey, List<Row>> rankedOfferMap = new HashMap<RankedOfferKey, List<Row>>();

		/*
		// Step 1. ETL aggregation
		if (Configuration.enableETL) {
			// updating today's offer wall actions/views, aggregation
			todayETLAgg(conn);
		}

		// Step 2: Segment aggregation
		// if (Configuration.Etl.RUN_IN_GLOBAL) {
		if (Configuration.Etl.RUN_ETL) {
			logger.debug("RUN_ETL<");
			// Global Offer Aggregation
			GlobalOfferAgg.execute(conn);
			CountryOfferAgg.execute(conn);
			//AppOfferAgg.execute(conn);
			//CountryAppOfferAgg.execute(conn);
			TJMOfferAgg.execute(conn);
			TJMCountryOfferAgg.execute(conn);
			logger.debug("RUN_ETL>");
		}
		// }
	  */
	  //commented by LJ 08-14

		// Step 3: Update the rankOfferMap for each Segment
		// do global optimization for generic offerwall
		if (Configuration.Ranking.OPT_GOW && Configuration.Ranking.OPT_GLOBAL) {
			GlobalOptOfferGenerator globalOptOfferGenerator = new GlobalOptOfferGenerator();

			// updating rankedOfferMap
			globalOptOfferGenerator.updateMap(conn, rankedOfferMap);
		}

	
		// do country optimization for generic offerwall
		if (Configuration.Ranking.OPT_GOW && Configuration.Ranking.OPT_COUNTRY) {
			Map<String, Integer> countriesLookbackMap = ConfigurationCountries.myConfiguration.lookBackDates;
			for (String country : countriesLookbackMap.keySet()) {
				OptOfferGenerator optOfferGenerator = new CountryOptOfferGenerator(
						country);

				// updating rankedOfferMap
				optOfferGenerator.updateMap(conn, rankedOfferMap);
			}
		}

		
		// do app optimization for generic offerwall
		/*
		if (Configuration.Ranking.OPT_GOW && Configuration.Ranking.OPT_APP) {
			Map<String, Integer> appLookbackMap = ConfigurationApp.myConfiguration.lookBackDates;
			for (String app : appLookbackMap.keySet()) {
				AppOptOfferGenerator offerGenerator = new AppOptOfferGenerator(
						app);

				// updating rankedOfferMap
				offerGenerator.updateMap(conn, rankedOfferMap);
			}
		}
		*/


		// do country&app optimization for generic offerwall
		/* Commented out based on Lin's change. Not part of the TEST !!!
		if (Configuration.Ranking.OPT_GOW && Configuration.Ranking.OPT_COUNTRY
				&& Configuration.Ranking.OPT_APP) {
			Map<String, Integer> countriesLookbackMap = ConfigurationCountries.myConfiguration.lookBackDates;
			Map<String, Integer> appLookbackMap = ConfigurationApp.myConfiguration.lookBackDates;
			for (String country : countriesLookbackMap.keySet()) {
				for (String app : appLookbackMap.keySet()) {
					OptOfferGenerator offerGenerator = new CountryAppOptOfferGenerator(
							country, app);

					// updating rankedOfferMap
					offerGenerator.updateMap(conn, rankedOfferMap);
				}
			}
		}
		*/ 
		
		// do the global optimization for tjm
		if (Configuration.Ranking.OPT_TJM && Configuration.Ranking.OPT_GLOBAL) {
			OptOfferGenerator offerGenerator = new TJMGlobalOptOfferGenerator();

			// updating rankedOfferMap
			offerGenerator.updateMap(conn, rankedOfferMap);
		}

		
		
		// do country optimization for TJM
		if (Configuration.Ranking.OPT_TJM && Configuration.Ranking.OPT_COUNTRY) {
			Map<String, Integer> countriesLookbackMap = ConfigurationCountries.myConfiguration.lookBackDates;
			for (String country : countriesLookbackMap.keySet()) {
				OptOfferGenerator optOfferGenerator = new TJMCountryOptOfferGenerator(
						country);

				// updating rankedOfferMap
				optOfferGenerator.updateMap(conn, rankedOfferMap);
			}
		}

		
		return rankedOfferMap;
	}



	@Override
	public void reloadDataResource() {
		Connection conn = null;

		// refreshing map
		try {
			conn = VerticaConn.getConnection();
			Date current = new Date();
			
			// Step 0: Refresh all Offer Map
			// Lei change note: Now this is managed by the globalDataManager
			//OfferCache.getInstance().resetMap(conn, "VerticaScore");
			
			
			// Step 1: Refresh Audition Files
			// Move up here due to the AUDITION DISCOUNT
			String auditionOutputPath = refreshAudition(current);  //commented by LJ

			// Step 2: Update the offer list
			// Runs approximately every hour
			if (refreshMapRunCount == 0) {
				refreshRankedOfferMap(conn);
			}

			refreshMapRunCount++;
			if (refreshMapRunCount > Configuration.Ranking.REFRESH_OFFER_COUNT) {
				refreshMapRunCount = 0;
			}
		
			// Step 3: update the data in the container for the compute engine
			((VerticaScoreResourceDataContainer)dataContainer).current = current;
			((VerticaScoreResourceDataContainer)dataContainer).rankedOfferMap = rankedOfferMap;
			((VerticaScoreResourceDataContainer)dataContainer).allOfferMap = OfferCache.getInstance().get();  //commented by LJ 08-16
			//((VerticaScoreResourceDataContainer)dataContainer).allOfferMap = OfferCache.getInstance().get(GlobalDataManager.conn);  //added by LJ
			HashMap<String, Map<String, Row>> allAuditionMaps = new HashMap<String, Map<String, Row>> ();
			
			for (String os: Configuration.OS.activeOs){
				for (int platform : Configuration.Platform.activePlatforms) {
					Map<String, Row> auditionMap = AuditionCache.getInstance().getAuditionMap(os, platform);
					allAuditionMaps.put(os + platform, auditionMap);
				}
			}
			((VerticaScoreResourceDataContainer)dataContainer).auditionOfferMaps = allAuditionMaps;
			
					
			
		} catch (SQLException e) {
			logger.error("MapRefreshJob SQLException:", e);
		} catch (IOException e) {
			logger.error("MapRefreshJob IOException:", e);  //commented by LJ associated with steps 1-2
		} catch (ClassNotFoundException e) {
			logger.error("MapRefreshJob ClassNotFoundException:", e);
		} catch (Throwable t) {
			logger.error("fatal throwable caught!!!", t);
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				logger.error("run failed!!!", e);
			}
		}
		
		
		logger.warn("Finished the RELOADING process");
	}


	protected HashSet<String> getStaticOfferListKeys(){
		return VerticaScoreResourceDataContainer.getInstance().staticOfferLists;
	}
	
	protected void setOfferListKeys(HashSet<String> offerListKeys){
		VerticaScoreResourceDataContainer.getInstance().staticOfferLists = offerListKeys;
	}
	
	protected String getIDKey(){
		return Configuration.IDKEY;
	}
	
	protected Logger getLogger(){
		return logger;
	}

}
