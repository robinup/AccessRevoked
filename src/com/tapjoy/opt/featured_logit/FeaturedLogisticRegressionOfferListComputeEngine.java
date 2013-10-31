package com.tapjoy.opt.featured_logit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.tapjoy.opt.cache.OfferListCache;
import com.tapjoy.opt.common.HBaseConn;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.featured_logit.config.Configuration;
import com.tapjoy.opt.object_cache.OfferCache;
import com.tapjoy.opt.offerlist.CompoundRow;
import com.tapjoy.opt.offerlist.OfferList;
import com.tapjoy.opt.offerlist.OfferListWithref;
import com.tapjoy.opt.resource.OfferListComputeEngine;
import com.tapjoy.opt.resource.ResourceDataContainer;


public class FeaturedLogisticRegressionOfferListComputeEngine extends OfferListComputeEngine {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(FeaturedLogisticRegressionOfferListComputeEngine.class);
	
	public FeaturedLogisticRegressionOfferListComputeEngine(ResourceDataContainer dataContainer) {
		super(dataContainer);
	}
	
	//Parses offer list file
	//Same as linear regression parse but doesn't have auditioning part
	@SuppressWarnings({ "resource", "unchecked" })
	private LinkedList<CompoundRow> parseOfferListFile(File file, JSONParser parser, Map<String, Row> allOfferMap){
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
				String offerid = offer.get("offer_id").toString();
				String score = offer.get("rank_score").toString().trim();
				Row offerRow = allOfferMap.get(offerid);
				if (offerRow == null) {
					System.out.println("Featured Logistic Regression parseOfferListFile()"+
							" -- No corresponding offer found " + 
							"in allOfferMap for offerId " + offerid);
					continue;
				}

				offerlistOfRows.add(new CompoundRow(offerid, score, "0", offerRow));
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("parseOfferListFile() -- Exception happend - " + e.getMessage());
			return null;
		}
		return offerlistOfRows;
	}
	
	//Computes the offerList that this specific device got
	//Takes static offer list and gets list based on biased coin function
	//Stores in cache
	//Request stores details of the incoming request (i.e. App ID, Library, Version, ...)
	public OfferList computeForDevice(String customizedKey, HashMap<String, String> request, OfferList staticOL){
		@SuppressWarnings("unchecked")
		List<CompoundRow> offers = (List<CompoundRow>)staticOL.getOffers();
		
		// Sort
		Collections.sort(offers, new Comparator<CompoundRow>(){
			public int compare(CompoundRow row1, CompoundRow row2) {
				/*if (row1.score > row2.score) {
					return -1;
				} else if (row1.score < row2.score) {
					return 1;
				}
				
				return 0;*/
				return new Double(row2.score).compareTo(row1.score);
			}
		});
		
		//Copy offers so that original list isn't changed
		List<CompoundRow> copiedOffers = new LinkedList<CompoundRow>();
		for(CompoundRow crow : offers) {
			copiedOffers.add(new CompoundRow(crow.id, Double.toString(crow.score), crow.source, crow.offer.clone()));
		}
		List<CompoundRow> chosen = biasedCoin(copiedOffers);
		return new OfferListWithref(customizedKey, chosen, true);
	}
	
	//Removes Zero Score offers just in case
	private void removeZeroScore(List<CompoundRow> offers) {
		LinkedList<CompoundRow> toRemove = new LinkedList<CompoundRow>();
		for(CompoundRow offer : offers) {
			if(offer.score == 0) {
				toRemove.add(offer);
			}
		}
		for(CompoundRow crow: toRemove) {
			offers.remove(crow);
		}
	}
	
	//Calculates the percentage the offer should be chose based on the offer list scores
	//Used with biasedCoin to pick the offers
	private HashMap<String,Float> calculatePercentages(List<CompoundRow> offers) {
		int totalScore = 0;
		for (CompoundRow offerrow : offers) {
			totalScore += offerrow.score;
		}
		HashMap<String,Float> percentages = new HashMap<String,Float>();
		for (CompoundRow offerrow : offers) {
			percentages.put(offerrow.id, (float)offerrow.score / totalScore);
		}
		return percentages;
	}
	
	//Randomly picks Offers from offer list
	//Cannot pick the same offer twice
	//Picks the number of offers specified by MAX_OFFERS in configuration file
	public List<CompoundRow> biasedCoin(List<CompoundRow> offers) {
		List<CompoundRow> choices = new LinkedList<CompoundRow>();
		this.removeZeroScore(offers);
		for(int i = 0; i < Configuration.MAX_OFFERS && offers.size() > 0; i++){
			Random rand = new Random();
			float rFloat = rand.nextFloat();
			HashMap<String,Float> percentages = calculatePercentages(offers);
			CompoundRow chosen = null;
			for (CompoundRow offerrow : offers) {
				if(rFloat < percentages.get(offerrow.id)) {
					chosen = offerrow;
					choices.add(chosen);
					break;
				}
				else {
					rFloat -= percentages.get(offerrow.id);
				}
			}
			offers.remove(chosen);
		}
		if(choices.size() > Configuration.MAX_OFFERS) {
			System.out.println(String.format("Did not find %d offers to pick", Configuration.MAX_OFFERS));
			return null;
		}
		return choices;
	}
	
	//Gets file from S3 and parses the file into OfferList object
	//Stores the OfferList in OfferListCache
	public boolean computeStaticSegments() {
		HashSet<String> offerListKeys = new HashSet<String>();
		//Connection conn = null;
		try {
			JSONParser parser = new JSONParser();
			//conn = VerticaConn.getConnection();
			//Map<String, Row> allOfferMap = OfferCache.getInstance().resetMap(conn, Configuration.IDKEY);
			Map<String, Row> allOfferMap = OfferCache.getInstance().get();		
			File featuredRegOfferlistDir = new File(Configuration.S3LocalCopyDir);
			if (featuredRegOfferlistDir == null || ! featuredRegOfferlistDir.isDirectory()) {
				System.out.println("Offer list directory is null or not a directory");
				return false;
			}
			
			File [] files = featuredRegOfferlistDir.listFiles();
			if (files.length == 0) {
				System.out.println("No files in directory");
				return false;
			}
			
			for (File file : files) {
				String key = file.getName();
				LinkedList<CompoundRow> offerlistOfRows = parseOfferListFile(file, parser, allOfferMap);
				OfferListWithref ol = new OfferListWithref(key, offerlistOfRows, true);
				offerListKeys.add(key);
				OfferListCache offerListCache = OfferListCache.getInstance();
				offerListCache.store(key, ol, true);
			}
			
			FeaturedLogisticRegressionResourceDataContainer.getInstance().staticOfferLists = offerListKeys;
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return false;
	}
	
	public ArrayList<String> rejectionOfferIds(String udid) {
		Result res = HBaseConn.getOneRecord(Configuration.HBASE_FEEDBACK, udid);
		if(res == null || res.isEmpty()) {
			return null;
		}
		for(KeyValue rawVal : res.raw()) {
			byte[] bytes = rawVal.getValue();
			String val = Bytes.toString(bytes);
			if(val == null || val.equals("null")) {
				continue;
			}
			val = val.replaceAll("(", "");
			val = val.replaceAll(")", "");
			val = val.replaceAll("{", "");
			val = val.replaceAll("}", "");
			String[] offerIds = val.split(",");
			return (ArrayList<String>) Arrays.asList(offerIds);
		}
		return null;
	}
	
}
