package com.tapjoy.opt.offerlist;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.object_cache.OfferCache;
import com.tapjoy.opt.offerfilter.OfferFilter;

public class OfferListWithref extends OfferList {
	/**
	 * Constructor 
	 * 
	 * @param keyForAllOfferMap - deprecated
	 */
	@SuppressWarnings("unchecked")
	public OfferListWithref(String key, List<Row> offerlistOfRows, boolean enabled, String label){
		this.key = key;
		this.enabled = enabled;
		
		Map<String, Row> allOfferMap = OfferCache.getInstance().get();  //commented by LJ
		//Map<String, Row> allOfferMap = OfferCache.getInstance().get(GlobalDataManager.conn);  //added by LJ
		
		//// ArrayList or LinkedList???
		this.offers =  new LinkedList<CompoundRow>();
		
		for (Row row : offerlistOfRows) {
			String id = row.getColumn(ID);
			String score = row.getColumn(RANK_ADJUSTED_SCORE);
			String source = row.getColumn(IS_AUDITION);
			Row offer = allOfferMap.get(id);
			if (offer == null) {
				System.out.println("no matching offer?! key:" + key + "; id:" + id);
				continue;
			}
			offers.add(new CompoundRow(id, score, source, offer));
		}
	}
	
	/**
	 * Constructor 
	 */
	public OfferListWithref(String key, boolean enabled) {
		this.key = key;
		this.enabled = enabled;
		
		Map<String, Row> allOfferMap = OfferCache.getInstance().get();
		String [] offerlists = (String[]) allOfferMap.keySet().toArray();
		
		//// ArrayList or LinkedList???
		this.offers =  new LinkedList<CompoundRow>();
		
		for (String id : offerlists) {

			Row offer = allOfferMap.get(id);
			if (offer == null) {
				System.out.println("no matching offer?! key:" + key + "; id:" + id);
				continue;
			}
			
			String score = offer.getColumn(RANK_ADJUSTED_SCORE);
			String source = offer.getColumn(IS_AUDITION);
			
			offers.add(new CompoundRow(id, score, source, offer));
		}
	}
	
	public OfferListWithref(String key, List<CompoundRow> offers, boolean enabled){
		this.key = key;
		this.enabled = enabled;
		this.offers = offers;
	}
	
	
	protected boolean processOneOffer(HashMap<String, String> requestSpec, int idx, StringBuffer buff, boolean isFirst, List<OfferFilter> filters){		
		CompoundRow crow = (CompoundRow)offers.get(idx);
		
		// Apply filters
		boolean passFilters = true;
		for (OfferFilter filter : filters) {
			if (! filter.isValid(crow)) {
				passFilters = false;
				break;
			}
		}
		
		if (! passFilters) {
			//System.out.println("LeiTest -- Row failed Filter: " + crow.toString());
			return false;
		}
		
		// Attach the result
		// Need to add the ","?
		if (isFirst) {
			buff.append("\n{");
		} else {
			buff.append(",\n{");
		}
		
		// Offer Id
		buff.append("\"id\":\"");
		buff.append(crow.id);
		buff.append("\",");
		// Index
		buff.append("\"rank_index\":");
		buff.append(idx + 1);
		buff.append(",");
		// Source  
		buff.append("\"auditioning\":");
		buff.append(crow.source);
		buff.append(",");
		// Score
		buff.append("\"rank_score\":");
		buff.append(crow.score);
		
		if(requestSpec.containsKey("test") && requestSpec.get("test").equals("true"))  //added for test urls 10-17
		{
			buff.append(",");
			buff.append("\"bid\":");
			buff.append(new Double(crow.getBid()).toString());
			buff.append(",");
			buff.append("\"offer_name\":");
			buff.append("\""+crow.getName()+"\"");
			buff.append(",");
			buff.append("\"item_type\":");
			buff.append("\""+crow.getItemType()+"\"");
			buff.append(",");
			buff.append("\"self_promote_only\":");
			buff.append(crow.getSelfPromoteOnly());	
			buff.append(",");
			buff.append("\"reserved_value\":");
			buff.append(new Double(crow.resv).toString());	
			buff.append(",");
			buff.append("\"reserved_string\":");
			buff.append("\""+crow.resstr+"\"");	
		}
		
		buff.append("}");
		
		return true;
	}


	@Override
	public boolean isTargetOfferId(String offerId, Object offer) {
		if (offerId.equals(((CompoundRow)offer).id)) {
			return true;
		}
		
		return false;
	}

	@Override
	public LinkedList<OfferListRowMC> toMcFormat() {
		if (! enabled) {
			return null;
		}
		
		LinkedList <OfferListRowMC> mcList= new LinkedList<OfferListRowMC>();
		for(Object row : offers){
			mcList.add(new OfferListRowMC((CompoundRow)row));
		}
		return mcList;
	}
	
}
