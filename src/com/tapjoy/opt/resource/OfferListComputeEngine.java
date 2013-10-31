package com.tapjoy.opt.resource;

import java.util.HashMap;

import com.tapjoy.opt.cache.OfferListCache;
import com.tapjoy.opt.offerlist.OfferList;

public abstract class OfferListComputeEngine {
	protected static OfferListCache offerCache = OfferListCache.getInstance();
	protected ResourceDataContainer dataContainer;
	
	public OfferListComputeEngine(ResourceDataContainer dataContainer){
		this.dataContainer = dataContainer;
	}
	
	// Generate device-customized Offer list. OK not to override it in subclasses
	public OfferList computeForDevice(String resultKey, HashMap<String, String> request, OfferList staticOL){
		return null;
	}
	
	// Generate publisher-customized Offer list. OK not to override it in subclasses
	public OfferList computeForPublisher(String request){
		return null;
	}
	
	public abstract boolean computeStaticSegments();

}
