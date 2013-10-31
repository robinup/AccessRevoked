package com.tapjoy.opt.offerfilter;

import java.util.HashMap;

import com.tapjoy.opt.common.Row;

public class TestRowOfferFilterOne extends OfferFilter {
	public boolean isValid(Row row, HashMap<String, String> requestSpec){
		if (row.getColumn(ID).contains("8c6b") || row.getColumn(PAYMENT) == null) {	
			return false;
		}
		
		return true;
	}

}
