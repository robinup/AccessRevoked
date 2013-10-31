package com.tapjoy.opt.offerlist;

import com.tapjoy.opt.common.Row;

public abstract class OfferFilter {
	// Each subclass should override at least one of the following method
	public boolean isValid(Row row){
		return true;
	}
	
	public boolean isValid(String offerId){
		return true;
	}
}
