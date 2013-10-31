package com.tapjoy.opt.offerlist;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.offerfilter.OfferFilter;

/**
 * 
 * @author lding
 * 
 * The OfferList used to store Static Offer lists. Each offer should contain all the 
 *   necessary information such that various filters can be applied  
 *
 */
public class OfferListFullLoad extends OfferList {
	
	@SuppressWarnings("unchecked")
	public OfferListFullLoad(String key, List<Row> offerlistOfRows, boolean enabled){
		this.key = key;
		this.enabled = enabled;
		
		//// ArrayList or LinkedList???
		this.offers =  new LinkedList<Row>();
		
		for (Row row : offerlistOfRows) {
			offers.add(row);
		}
	}
	
	protected boolean processOneOffer(HashMap<String, String> requestSpec, int idx, StringBuffer buff, boolean isFirst, List<OfferFilter> filters){		
		Row row = (Row)offers.get(idx);
		
		// Apply filters
		boolean passFilters = true;
		for (OfferFilter filter : filters) {
			if (! filter.isValid(row)) {
				passFilters = false;
				break;
			}
		}
		
		if (! passFilters) {
			//System.out.println("LeiTest -- Row failed Filter: " + row.toString());
			return false;
		}
		
		// Attach the result
		if (isFirst) {
			buff.append("\n{");
		} else {
			buff.append(",\n{");
		}
		
		// Offer Id
		buff.append("\"id\":\"");
		buff.append(row.getColumn(ID));
		buff.append("\",");
		// Index
		buff.append("\"rank_index\":");
		buff.append(idx + 1);
		buff.append(",");
		// Source  
		buff.append("\"auditioning\":");
		buff.append(row.getColumn(IS_AUDITION));
		buff.append(",");
		// Score
		buff.append("\"rank_score\":");
		buff.append(row.getColumn(RANK_ADJUSTED_SCORE));
		
		buff.append("}");
		
		return true;
	}


	@Override
	public boolean isTargetOfferId(String offerId, Object offer) {
		if (offerId.equals(((Row)offer).getColumn(ID))) {
			return true;
		}
		
		return false;
	}

	@Override
	public LinkedList<OfferListRowMC> toMcFormat() {
		// TODO Auto-generated method stub
		return null;
	}

}
