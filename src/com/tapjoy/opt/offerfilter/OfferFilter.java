package com.tapjoy.opt.offerfilter;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.offerlist.CompoundRow;

public abstract class OfferFilter implements ColumnDef {
	// Each subclass should override at least one of the following method
	// For Fully loaded offer records
	public boolean isValid(Row row){
		return true;
	}
	
	// For lightweight offer records
	public boolean isValid(String offerId){
		return true;
	}
	
	public boolean isValid(CompoundRow crow){
		return isValid(crow.offer);
	}
}
