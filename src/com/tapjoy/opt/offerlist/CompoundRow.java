package com.tapjoy.opt.offerlist;

import java.util.Comparator;
import java.util.Map;

import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.ColumnDef;

public class CompoundRow {
	public String id;
	public String source;  // Is this offer from audition?
	public double score;
	public Row offer;
	public double resv; //reserved value added 10-17 LJ
	public String resstr;  //reserved string added 10-17 LJ
	
	public CompoundRow(String id, String score, String source, Row offer){
		this.id = id;
		this.source = source;
		this.score = Double.parseDouble(score);
		this.offer = offer;
		this.resv = 0.0;
		this.resstr = new String();
	}
	
	public CompoundRow(CompoundRow cr) {
		this.id = cr.id;
		this.source = cr.source;
		this.score = cr.score;
		this.offer = cr.offer;
		this.resv = 0.0;
		this.resstr = new String();
	}

	// Restoring from the MC format
	public CompoundRow(OfferListRowMC rowmc, Map<String, Row> allOfferMap){
		this.id = rowmc.id;
		this.source = rowmc.source;
		this.score = Double.parseDouble(rowmc.score);
		this.offer = allOfferMap.get(id);
		this.resv = 0.0;
		this.resstr = new String();
	}
	
	public static class CompoundRowCompare implements Comparator<CompoundRow>  //added by LJ
	{
		@Override
		public int compare(CompoundRow o1, CompoundRow o2) {
			// TODO Auto-generated method stub
			return new Double(o1.score).compareTo(o2.score);			
		}
	}
	
	public static class CompoundRowNewCompare implements Comparator<CompoundRow>  //added by LJ
	{
		@Override
		public int compare(CompoundRow o1, CompoundRow o2) {
			// TODO Auto-generated method stub
			return new Double(o1.resv).compareTo(o2.resv);			
		}
	}
	
	public double getBid()  //new API added Oct 9
	{
		return Double.parseDouble(offer.getColumn(ColumnDef.BID));
	}
	
	public String getName()  //new API added Oct 17
	{
		return offer.getColumn(ColumnDef.OFFER_NAME);
	}
	
	public String getItemType()  //new API added Oct 17
	{
		return offer.getColumn(ColumnDef.ITEM_TYPE);
	}
	
	public String getSelfPromoteOnly()
	{
		return offer.getColumn(ColumnDef.SELF_PROMOTE_ONLY);
	}
}
