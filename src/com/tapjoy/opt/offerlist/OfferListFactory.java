package com.tapjoy.opt.offerlist;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.tapjoy.opt.common.Row;

public class OfferListFactory {
	public static OfferList restoreFromMcObject(String key, boolean enabled, List<OfferListRowMC> mcObj, Map<String, Row> allOfferMap) {
		if (key == null || mcObj == null || mcObj.size() == 0) {
			return null;
		}
		
		// Use the first element to decide what kind of offferList should be created
		Object firstElement = mcObj.get(0);
		if (firstElement == null) {
			return null;
		}
		
		LinkedList<CompoundRow> ol = new LinkedList<CompoundRow>();
		for(OfferListRowMC item : mcObj) {

			if (item.id == null || item.source == null || allOfferMap.get(item.id) == null) {
				continue;
			}
			ol.add(new CompoundRow(item, allOfferMap)); 
		}

		return new OfferListWithref(key, ol, true); 
	}

	
	// TODO -- logics for creating OfferListFullLoad and OfferListLightweight from OfferListRowMC
}
