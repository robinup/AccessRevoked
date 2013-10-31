package com.tapjoy.opt.vertica_score;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.resource.ResourceDataContainer;
import com.tapjoy.opt.vertica_score.entity.RankedOfferKey;

public class VerticaScoreResourceDataContainer extends ResourceDataContainer {
	protected Map<RankedOfferKey, List<Row>> rankedOfferMap;
	protected Map<String, Row> allOfferMap;
	protected Map<String, Map<String, Row>> auditionOfferMaps;
	// This Date obj is used to sync the file directories between different stages
	protected Date current; 
	
	public static VerticaScoreResourceDataContainer instance = new VerticaScoreResourceDataContainer();

	private VerticaScoreResourceDataContainer() {
		this.identity = "VerticaScore";
	}
	
	public static VerticaScoreResourceDataContainer getInstance(){
		return instance;
	}
	
	public Map<RankedOfferKey, List<Row>> getRankedOfferMap(){
		return rankedOfferMap;
	}

	public Map<String, Row> getAllOfferMap(){
		return allOfferMap;
	}
	
	public Map<String, Row> getAuditionMap(String os, String Platform){
		return auditionOfferMaps.get(os + Platform);
	}
	
	public Date getCurrent(){
		return current;
	}
}
