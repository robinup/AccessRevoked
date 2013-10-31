package com.tapjoy.opt.offerfilter;

import java.util.HashMap;

import com.tapjoy.opt.common.Row;

public class StartOfferFilter extends OfferFilter {
	private String startOffer;
	private boolean isValid = false;
	
	public void initiate(HashMap<String, String> requestSpec){
		startOffer = requestSpec.get("sidx");
		if (startOffer == null) {
			isValid = true;
		}
		else {
			isValid = false;
		}
	}
	
	public boolean isValid(Row row){
		if (isValid) {
			return true;
		}
		
		if (startOffer.equals(row.getColumn(ID))) {
			isValid = true;
			return false;
		}
		
		return false;
	}
	
	public boolean isValid(String offerId){
		if (isValid) {
			return true;
		}
		
		if (startOffer.equals(offerId)) {
			isValid = true;
			return false;
		}
		
		return false;
	}

}
