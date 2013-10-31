package com.tapjoy.opt.resource;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.OperationFuture;

import org.apache.log4j.Logger;

import com.tapjoy.opt.cache.OfferListCache;
import com.tapjoy.opt.config.OverallConfig;
import com.tapjoy.opt.offerfilter.OfferFilter;
import com.tapjoy.opt.offerfilter.RowOfferFilterForCountry;
import com.tapjoy.opt.offerlist.OfferList;




/**
 * 
 * @author lding
 * 
 *         A Data Resource is a collection of data/algo that are required for
 *         generating the OfferLists
 * 
 *         Each Data Resource defines its own Data Formats, Origin (Vertica,
 *         Memcached, HBase, etc) and Methodology (score based, model based,
 *         etc. May further contain different algos)
 * 
 */
public abstract class ResourceManager {
	protected ResourceDataContainer container;
	protected ResourceUpdateManager updateMgr;
	protected OfferListComputeEngine computeEngine;
	
	protected int dataResourceVersion = 1;
	protected String MCKEY_BASE = "";
	private MemcachedClient mcClient;

	// For each offer list engine that serves customized offer lists, they may need 
	//   an backup engine for static offer list to fail over to.
	protected ResourceManager backupRsrcMgr;

	// Number of seconds between data reloading cycles
	protected int RELOAD_DELAY = 300;

	// Is this offer list finished initialization and ready to serve requests?
	private boolean status = false;
	
	// The filters to apply for different requests. 
	protected List<OfferFilter> sharedFilters;
	
	private static Logger logger = Logger.getLogger(ResourceManager.class);

	public ResourceManager() {
		initialize();
		setupSharedFilters();
		//setupCronJob();  // LeiTest - hack!!
	}
	
	// If the resource manager depends on some command line options, override this function
	// Because the command parser can't be executed at multi levels. we have to wrap the args 
	//   into a commandline Obj and pass it around
	
	// Expose the compute engine for request handlers
	public OfferListComputeEngine getComputeEngine(){
		return computeEngine;
	}
	
	// Doing the constructor jobs here 
	protected abstract void initialize();

	/**
	 * The Updater should call this after the static offer lists are ready
	 */
	public void setServeStatus(boolean status) {
		this.status = status;
	}

	/**
	 * Determines if the server is ready to serve requests.
	 * 
	 * Start the Netty Engine only when this returns true
	 */
	public boolean getServeStatus() {
		return status;
	}
	
	/**
	 * For customizable offer list engines, Each request may need to map to one of the static 
	 * @param specs
	 * @return
	 */
	protected String getStaticOfferListKey(HashMap<String, String> specs) {
		return null;
	}
	
	/**
	 * Based on the parameters of the request, decide the key to the offer list
	 * @param specs -- the request parameters. Offerlist engines use this to compose a offerlist name
	 * @params asStaticEngine -- when this offerlist engines is used as the Static offerlist Engine by
	 *                           another offerlist engine, some params of "specs" may need to be specially 
	 *                           processed                        
	 * @return
	 * 
	 * Override one of the following two methods when necessary. Once this part is stable, move it to higher level   
	 */
	public abstract String getOfferListKey(HashMap <String, String> specs, boolean asStaticEngine);
	
	
	
	/**
	 * For customized offerlist engines, if they are using another offerlist engine to provide a 
	 * @param defaultKey
	 * @param specs
	 * @return
	 */
	public abstract String getOfferListKey(String defaultKey, HashMap <String, String> specs);
	
	
	/**
	 * Implement the request serve logic here
	 * 
	 * @param ctx
	 * @param requests
	 * 
	 * Override this function in individual OfferList engines when necessary
	 */
	protected OfferList retrieveOrCreateOfferList(HashMap <String, String> specs) {
		// Parse the request to the corresponding offer list
		String key = getOfferListKey(specs, false);
		if (key == null ) {
			logger.error("Requests mapped to empty key");
			return null;
		}
		
		//logger.debug("LeiTest -- the mapped key is " + key);
		OfferList list = OfferListCache.getInstance().retrieve(key, true);
		if(list != null)
		{
			//System.out.printf("retrieveOrCreateOfferList returns %d offers from sCache \n", list.size());
			return list;
		}
		else
		{
			list = OfferListCache.getInstance().retrieve(key, false);
			/*if (list != null)
				System.out.printf("retrieveOrCreateOfferList returns %d offers from vCache \n", list.size());
			else
				System.out.printf("retrieveOrCreateOfferList returns null from vCache \n");*/
			return list;
		}
	}
	
	
	/**
	 *  Set up filters that are shared by every requests. Those filters can not be initialized!
	 *  
	 *  Override this function when necessary
	 */
	protected void setupSharedFilters(){
		sharedFilters = new LinkedList<OfferFilter>();
	}
	
	/**
	 * A list of filters that need to be initialized based on the Client request
	 * @param specs
	 * @return
	 */
	protected LinkedList<OfferFilter> setupPrivateFilters(HashMap <String, String> specs){
		LinkedList<OfferFilter> privateFilters = new LinkedList<OfferFilter>();
		privateFilters.add(new RowOfferFilterForCountry(specs));
		
		return privateFilters;
	}

	public String serveRequest(String command, HashMap <String, String> specs) {
		if ("offerwall".equals(command)) {
			OfferList ol = retrieveOrCreateOfferList(specs);
			
			if (ol == null) {
				return "Invalid Request. ErrCode 2";
			} 
			
			// Initiate private filters
			LinkedList<OfferFilter> privateFilters = setupPrivateFilters(specs);
			
			// Get return size
			String offerCount = specs.get("offer_count");
			if (offerCount == null) {
				return "Invalid Request. ErrCode 3";
			}
			
			int offerCnt = Integer.parseInt(offerCount);
			if (offerCnt < 1) {
				return "Invalid Request. ErrCode 3";
			}
			
			// Serve request
			int startIdx = ol.decideStartIdx(specs.get("sidx"));
			String result = ol.serve(specs, startIdx, offerCnt, privateFilters);
			
			return result;
		}
		
		// Not a valid command
		return "Invalid Request. ErrCode 1";
	}
	
	public synchronized MemcachedClient getMCClient(){
		try {
			if (mcClient == null) {
				mcClient = new MemcachedClient(new InetSocketAddress(OverallConfig.MEMCACHE_ADDR, OverallConfig.MEMCACHE_PORT));
			}

			return mcClient;
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}
	
	
	/**
	 * Update the offer list engine 
	 */
	public boolean refresh() {
		updateMgr.reloadDataResource();
		computeEngine.computeStaticSegments();
		return true;
	}
	
	
	/**
	 * This function is used to share the reloadResource result with other servers
	 * 
	 * When it's time to update the data in the data container, the basic algo is:
	 * 1)  Check the latest version number in MC. If version number is bigger than 
	 *     the current one, go to step 2), else go to step 3)
	 * 
	 * 2)  importDataResource() to import the data from MC. update version number, 
	 *     then go to step 8)
	 * 
	 * 3)  Lock the updateOngoing key. If failed, sleep 5 seconds then restart 
	 *     step 1), if succeed, continue
	 * 
	 * 4)  call refresh() to reload external resources and recalculate the static offer lists
	 * 
	 * 5)  call exportDataResource() to dump data to MC
	 * 
	 * 6)  increase the version number, also update in MC
	 * 
	 * 7)  unlock the updateOngoing key
	 * 
	 * 8)  End. 
	 *
	 * For MC operations, except the Locking stage, nothing is critical
	 */
	public void refreshAndSync() {
		int retry = 0;
		MemcachedClient mcClt = getMCClient();
	
		while (retry < 2) {
			Integer versionMC = (Integer) mcClt.get(MCKEY_BASE + ":version");
			if (versionMC != null) {
				logger.info("The MC version: " + versionMC.intValue() + " -- " + MCKEY_BASE);
				if (versionMC.intValue() > dataResourceVersion) {
					logger.info("Import the Engine data -- " + MCKEY_BASE);		
					boolean importStatus = updateMgr.importDataResource(mcClt);

					retry++;
					if (importStatus) {
						dataResourceVersion = versionMC.intValue();
						logger.info("Import the Engine data finished successfully -- " + MCKEY_BASE);
						break;
					}

					logger.error("ImportDataResource FAILURE");

					continue;
				}
			}

			logger.info("Lock the Engine update process -- " + MCKEY_BASE);
			OperationFuture<java.lang.Boolean> lockResult = mcClt.add(MCKEY_BASE + ":reloadLock", 3600, "LOCK");
			try {
				if (!lockResult.get(100, TimeUnit.MILLISECONDS).booleanValue()) {
					logger.warn("Set lock failed! ");
					Thread.sleep(5000);
					continue;
				}
			} catch (Exception e) {
				continue;
			}

			retry ++;
			logger.info("Refresh the Engine data -- " + MCKEY_BASE);
			refresh();
			logger.info("Export the Engine data to MC -- " + MCKEY_BASE);
			boolean exporstStatus = updateMgr.exportDataResource(mcClt);

			// Only update the version ID when export is done successfully
			if (exporstStatus) {
				dataResourceVersion++;
				logger.info("Updating Engine data version ID to " + dataResourceVersion + " -- " + MCKEY_BASE);
				OperationFuture<java.lang.Boolean> setResult = mcClt.set(MCKEY_BASE + ":version", OverallConfig.MAX_RELOAD_INTERVAL,
						new Integer(dataResourceVersion));
				try {
					if (! setResult.get(100, TimeUnit.MILLISECONDS)) {
						logger.error("ERROR - Updating version number to MC TIMEOUT");
					}
				} catch (Exception e) {
					logger.error("ERROR - Updating version number to MC FAILURE" + e.getMessage());
				}
			} else {
				logger.error("ERROR - exportDataResource FAILURE");
			}
			
			logger.info("Clear Lock -- " + MCKEY_BASE);
			mcClt.delete(MCKEY_BASE + ":reloadLock");
			logger.info("Engine Update finished successfully -- " + MCKEY_BASE);

			if (exporstStatus) {
				break;
			}
			continue;
		}

	}
	
	
	/**
	 * The data update thread
	 * 
	 */
	public class UpdateCronJob implements Runnable {

		@Override
		public void run() {
			logger.info("Reloading Resource -- " + MCKEY_BASE);

			refresh();
			//refreshAndSync();

			setServeStatus(true);
		}
	}

	/**
	 * Setup the updater cronjob
	 */
	protected void setupCronJob() {
		//System.out.println("LeiTest -- RELOAD_DELAY: " + RELOAD_DELAY + "; " + this.getClass().getCanonicalName());
		ScheduledExecutorService scheduler = Executors
				.newSingleThreadScheduledExecutor();

		scheduler.scheduleWithFixedDelay(new UpdateCronJob(), 0, RELOAD_DELAY, SECONDS);
	}
}
