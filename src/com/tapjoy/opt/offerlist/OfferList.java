package com.tapjoy.opt.offerlist;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.offerfilter.OfferFilter;

public abstract class OfferList implements ColumnDef {

	protected String key;
	protected boolean enabled;
	@SuppressWarnings("rawtypes")
	protected List offers;

	
	public String getKey() {
		return key;
	}
	
	@SuppressWarnings("rawtypes")
	public List getOffers() {
		return offers;
	}
	
	public int size() {
		return offers.size();
	}
	
	
	private String emptyResult(){
		StringBuffer buff = new StringBuffer();
		
		buff.append("{\n        \"key\":\"");
	
		// append key
		buff.append(key);
	
		buff.append("\",\n        \"enabled\":\"");
	
		if (enabled) {
			buff.append("true");
		} else {
			buff.append("false");
		}
	
		buff.append("\",\n        \"offers\":\n        [");
		buff.append("\n        ]\n}\n");
		
		return buff.toString();
	}

	public String serve(HashMap<String, String> requestSpec, int startIdx, int size, List<OfferFilter> filters) {
		if (startIdx >= offers.size() || size <= 0) {
			return emptyResult();
		}
		
		return serveJsonString(requestSpec, startIdx, size, filters);
	}


	protected String serveJsonString(HashMap<String, String> requestSpec, int startIdx, int size, List<OfferFilter> filters) {
		StringBuffer buff = new StringBuffer();
	
		buff.append("{\n        \"key\":\"");
	
		// append key
		buff.append(key);
	
		buff.append("\",\n        \"enabled\":\"");
	
		if (enabled) {
			buff.append("true");
		} else {
			buff.append("false");
		}
	
		buff.append("\",\n        \"offers\":\n        [");
	
		boolean isFirst = true;
		int count = 0;
		for (int i=startIdx; i<offers.size(); i++) {
			if (isFirst && count > 0) {
				isFirst = false;
			}
			
			if(! processOneOffer(requestSpec, i, buff, isFirst, filters)){
				continue;
			}
			
			count ++;
			if (count >= size) {
				break;
			}
		}
	
		buff.append("\n        ]\n}\n");
		
		return buff.toString();
	}
	
	public int decideStartIdx(String offerId) {
		if (offerId == null) {
			return 0;
		}
		
		for (int i = 0; i<offers.size(); i++) {
			if (isTargetOfferId(offerId, offers.get(i))) {
				return i + 1;
			}
		}
		
		return 0;
	}
	
	public abstract boolean isTargetOfferId(String offerId, Object offer);
	
	protected abstract boolean processOneOffer(HashMap<String, String> requestSpec, int idx, StringBuffer buff, boolean isFirst, List<OfferFilter> filters);

	public abstract LinkedList<OfferListRowMC> toMcFormat();
	
}