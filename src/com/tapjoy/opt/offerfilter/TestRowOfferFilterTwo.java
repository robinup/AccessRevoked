package com.tapjoy.opt.offerfilter;

import java.util.HashMap;

import com.tapjoy.opt.common.Row;

public class TestRowOfferFilterTwo extends OfferFilter {
	public boolean isValid(Row row, HashMap<String, String> requestSpec){
		if ("4".equals(row.getColumn(ID).substring(0, 1)) || Double.parseDouble(row.getColumn(RANK_ADJUSTED_SCORE)) % 1 >= 0.8) {		
			return false;
		}
		
		return true;
	}

}