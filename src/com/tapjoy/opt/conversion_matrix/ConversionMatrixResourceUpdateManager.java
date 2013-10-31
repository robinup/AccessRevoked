package com.tapjoy.opt.conversion_matrix;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.log4j.Logger;

import com.tapjoy.opt.common.HBaseConn;
import com.tapjoy.opt.conversion_matrix.config.Configuration;
import com.tapjoy.opt.resource.ResourceDataContainer;
import com.tapjoy.opt.resource.ResourceUpdateManager;

public class ConversionMatrixResourceUpdateManager extends
		ResourceUpdateManager {

	public ConversionMatrixResourceUpdateManager(
			ResourceDataContainer dataContainer) {
		super(dataContainer);
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void reloadDataResource() {
		System.out.println("LeiTest -- ConvMatrix loading ...");
		
		// Load the conversion matrix as a two-D map
		HashMap <String, HashMap> conversionMatrix = new HashMap<String, HashMap>();
		
		long baseTimeStamp = -1;

		ResultScanner rs = HBaseConn.getAllRecord("OfferMatrix", 1000);
		for(Result r:rs){
			// Is this the desired field - make sure the timestamp is new 
			KeyValue kv = r.getColumnLatest("score".getBytes(), "val".getBytes());
			if (kv == null || kv.getTimestamp() <= baseTimeStamp ) {
				continue ; 
			}

			String rowKey = new String(r.getRow());
			float score = Float.parseFloat(new String(kv.getValue()));

			String [] udids = rowKey.split("#");
			if (udids.length == 2) {
				@SuppressWarnings({ "unchecked"})
				HashMap <String, Float> perOffer = (HashMap <String, Float>)conversionMatrix.get(udids[0]);
				if (perOffer == null ) {
					perOffer = new HashMap<String, Float>();
					conversionMatrix.put(udids[0], perOffer);
				}
				perOffer.put(udids[1], score);
				
				//commented because it is conditional probability right now, not bidirectional - LJ
				/*HashMap <String, Float> pairOffer = (HashMap <String, Float>)conversionMatrix.get(udids[1]);
				if (pairOffer == null ) {
					pairOffer = new HashMap<String, Float>();
					conversionMatrix.put(udids[1], pairOffer);
				}
				if(!pairOffer.containsKey(udids[0]))
					pairOffer.put(udids[0], score);*/
			}
		}
     
		((ConversionMatrixResourceDataContainer)dataContainer).conversionMatrix = conversionMatrix;
		System.out.println("LeiTest -- ConvMatrix - the matrix size " + conversionMatrix.size());
				
		((ConversionMatrixResourceDataContainer)dataContainer).rttable = HBaseConn.initRTTable(Configuration.RT_TABLE_NAME);
		((ConversionMatrixResourceDataContainer)dataContainer).auxtables = new ArrayList<HTableInterface>();
		((ConversionMatrixResourceDataContainer)dataContainer).auxtables.add(((ConversionMatrixResourceDataContainer)dataContainer).rttable);
		
		for(String tablestr: Configuration.AUX_TABLE_NAMES)
		{
			((ConversionMatrixResourceDataContainer)dataContainer).auxtables.add(HBaseConn.initTable(tablestr));
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
