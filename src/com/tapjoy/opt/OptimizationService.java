package com.tapjoy.opt;

//import java.net.InetSocketAddress;

import java.io.IOException;
import java.util.HashSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;

//import net.spy.memcached.MemcachedClient;

import com.tapjoy.opt.common.GeoIpService;
import com.tapjoy.opt.common.HBaseConn;
import com.tapjoy.opt.config.OverallConfig;
import com.tapjoy.opt.config.DynamicConfig;
import com.tapjoy.opt.resource.ResourceManager;
import com.tapjoy.opt.server.ServiceEngine;
import com.tapjoy.opt.model_assigner.AssignerManager;


public class OptimizationService {
	private static Logger logger = Logger.getLogger(OptimizationService.class);
	
	private static GlobalDataManager globalDataManager; 
	
	public static final HashSet<String> platform_whitelist = new HashSet<String>();
	
	private static void initWhiteList()
	{
		String[] whitelist = {"ios", "android","windows", "windows_phone","windowsphone","wp", "windows-phone"};
		for(String str: whitelist)
			platform_whitelist.add(str);
	}
	
	private static CommandLine processTopLevelArgs(String [] args){
		try {
			// create Options object
			Options options = new Options();

			// Top level options
			options.addOption("p", "production", true,
					"production or not (y/n)");

			options.addOption("s", "productionS3", true,
					"productionS3 or not (y/n)");
			
			
			// VerticaScore engine options
			options.addOption("d", "databaseServer", true,
					"database going to be used");

			@SuppressWarnings("static-access")
			Option option = OptionBuilder.withLongOpt("algorithms").hasArgs()
					.withDescription("algorithms to be executed").create('a');
			options.addOption(option);		
						
			CommandLineParser parser = new PosixParser();
			CommandLine cmd = parser.parse(options, args);
	
			if (cmd.hasOption("production") == false) {
				logger.fatal("missing production argument");
				return null;
			} else {
				String productionCfg = cmd.getOptionValue("production");
				if (productionCfg.equals("y")) {
					// setting the connection to production
					OverallConfig.setProduction();
				}
			}


			if (cmd.hasOption("productionS3") == false) {
				logger.error("missing productionS3 argument");
			} else {
				String productionS3Cfg = cmd.getOptionValue("productionS3");
				if (productionS3Cfg.equals("y")) {
					OverallConfig.setProductionS3();
				}
			}
			
			if (cmd.hasOption("databaseServer") == false) {
				logger.error("missing databaseServer argument");
			} else {
				OverallConfig.setDatabaseServer(cmd.getOptionValue("databaseServer"));
			}

		
			
			return cmd;
		} catch (ParseException exp) {
			logger.fatal("Unexpected exception:" + exp.getMessage());
		}
		
		return null;
	}
	
	// Load the external data, initialize service engines
	@SuppressWarnings("static-access")
	private static void initializeAll(String [] args){
		CommandLine cmd = processTopLevelArgs(args);
		
		// Initialize the general resources
		GeoIpService.initialize();
		
		//before HBase init, try ping the server 
		HBaseConn.init();  //changed by LJ  //temporarily commented out 09/20
		
		initWhiteList();
		
		// Start the global data manager
		globalDataManager = new GlobalDataManager();
		globalDataManager.setupCronJob();
		
		logger.info("DataMgr init");
		// Wait for the globalDataManager before starting offerlist engines
		try {
			while (globalDataManager.status != true) {
				Thread.sleep(10000);
				logger.info("Waiting for global data manager initial run");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Start each offer list engine (ResourceManager)
		ModelController.startAllReg(cmd);
		AssignerManager.preloadAssigner();
	}
	
	//added by LJ -- a monitoring class for dynamic configuration
	public static class mthread implements Runnable
	{

		public mthread()
		{
			super();
		}
		
		@Override
		public void run() {
			try {
				DynamicConfig.initDynamicConfig();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Do initialization works
		initializeAll(args);
		
		//initiate dynamic configuration
		mthread rc = new mthread();
		
		Thread mt = new Thread(rc);
		mt.start();
		
		// Wait for the Resource Manager to be ready
		try {
			while(true)
			{
				boolean flag = true;
				for(ResourceManager mgr :ModelController.getAllRegModels())
				{
					if(mgr.getServeStatus() != true)
					{
						flag = false;
						break;
					}
				}		
				if(!flag)
				{
					Thread.sleep(10000); // LeiTest
					logger.info("Waiting for resource manager initialization");
				}
				else
					break;
			}
			
		} catch (Exception e) {
				//let watchdog handle this exception by restarting the service - LJ
		}
	
		// Start OptSOA to accept requests, register the shutdown hook
		final ServiceEngine engine = ServiceEngine.getInstance();
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run(){
				engine.shutDown();
			}
		});
		
		//HBaseConn.shutdown();
	}

}
