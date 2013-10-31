package com.tapjoy.opt.linear_regression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.tapjoy.opt.cache.OfferListCache;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.linear_regression.config.Configuration;
import com.tapjoy.opt.object_cache.OfferCache;
import com.tapjoy.opt.offerlist.CompoundRow;
import com.tapjoy.opt.offerlist.OfferListWithref;
import com.tapjoy.opt.resource.OfferListComputeEngine;
import com.tapjoy.opt.resource.ResourceDataContainer;

public class LinearRegressionOfferListComputeEngine extends OfferListComputeEngine {
	private static Logger logger = Logger.getLogger(LinearRegressionOfferListComputeEngine.class);

	public LinearRegressionOfferListComputeEngine(ResourceDataContainer dataContainer) {
		super(dataContainer);
	}
	
	/**
	 * Parse an offerlist result file to a List of CompoundRow
	 * @param file
	 * @param parser
	 * @param allOfferMap
	 * @return
	 */
	@SuppressWarnings({ "resource", "unchecked" })
	private LinkedList<CompoundRow> parseOfferListFile(File file, JSONParser parser, 
			Map<String, Row> allOfferMap){
		LinkedList<CompoundRow> offerlistOfRows = new LinkedList<CompoundRow>();
		StringBuilder  stringBuilder = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader( new FileReader (file));
			String line = null;
			while( ( line = reader.readLine() ) != null ) {
				stringBuilder.append( line );
			}
			
			Object obj = parser.parse(new FileReader(file));
			JSONObject jsonObject = (JSONObject) obj;
			JSONArray offers = (JSONArray) jsonObject.get("offers");
			Iterator<Object> iterator = offers.iterator();
			while (iterator.hasNext()) {
				JSONObject offer = (JSONObject)iterator.next();
				String offerid = (String)offer.get("offer_id");
				String score = offer.get("rank_score").toString();
				String audition = (String)offer.get("auditioning");
				String source = "true".equals(audition) ? "1" : "0";
				Row offerRow = allOfferMap.get(offerid);
				if (offerRow == null) {
					logger.warn("parseOfferListFile() -- No corresponding offer found " + 
							"in allOfferMap for offerId " + offerid);
					continue;
				}
				
				offerlistOfRows.add(new CompoundRow(offerid, score, source, offerRow));			
				//System.out.println("LeiTest -- offer_id: " + offerid + "; score:" + score);
			}
		} catch (Exception e) {
			logger.error("parseOfferListFile() -- Exception happend - " + e.getMessage());
			return null;
		}
		
		return offerlistOfRows;
	}

	@Override
	// TODO -- add status check for this function
	public boolean computeStaticSegments() {
		//logger.debug("LeiTest -- LinearRegressionOfferListComputeEngine -- ");
		
		// For each of the offer list files downloaded from S3, create an OfferListWithRef Obj and 
		//   store that Obj to the Persistent Cache	
		HashSet<String> offerListKeys = new HashSet<String>();
		//Connection conn = null;
		try {
			JSONParser parser = new JSONParser();
			//conn = VerticaConn.getConnection();
			//Map<String, Row> allOfferMap = OfferCache.getInstance().resetMap(conn, Configuration.IDKEY);
			/*if(OfferCache.getInstance().get() != null)
			{
				 System.out.printf("OfferCache of linear_regression size=%d\n", OfferCache.getInstance().get().size());
				 for(StackTraceElement ste: Thread.currentThread().getStackTrace())
					 System.out.println(ste);
			}
			else
				System.out.printf("OfferCache of linear_regression is null\n");*/
			Map<String, Row> allOfferMap = OfferCache.getInstance().get();		
			File linearRegOfferlistDir = new File(Configuration.S3LocalCopyDir);
			if (linearRegOfferlistDir == null || ! linearRegOfferlistDir.isDirectory()) {
				return false;
			}
			
			File [] files = linearRegOfferlistDir.listFiles();
			if (files.length == 0) {
				return false;
			}
			
			for (File file : files) {
				String key = file.getName();
				logger.info("computeStaticSegments()--offerlist for key: " + key);
				LinkedList<CompoundRow> offerlistOfRows = parseOfferListFile(file, parser, allOfferMap);
				OfferListWithref ol = new OfferListWithref(key, offerlistOfRows, true);
				offerListKeys.add(key);
				OfferListCache offerListCache = OfferListCache.getInstance();
				offerListCache.store(key, ol, true);
			}
			
			LinearRegressionResourceDataContainer.getInstance().staticOfferLists = offerListKeys;
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return false;

	}
}
