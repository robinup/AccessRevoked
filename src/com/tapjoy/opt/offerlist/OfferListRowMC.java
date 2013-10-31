package com.tapjoy.opt.offerlist;

import java.io.Serializable;

@SuppressWarnings("serial")
public class OfferListRowMC implements Serializable {
	public String id;
	public String source;  // Is this offer from audition?
	public String score;
	
	public OfferListRowMC(String id, String source, String score){
		this.id = id;
		this.source = source;
		this.score = score;
	}
	
	public OfferListRowMC(CompoundRow row){
		this.id = row.id;
		this.source = row.source;
		this.score = "" + row.score;
	}
}
